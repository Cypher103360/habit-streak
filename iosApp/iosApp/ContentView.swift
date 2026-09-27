import SwiftUI
import UserNotifications
import SharedLogic

// MARK: - Swift Observable ViewModel Wrapper
class ObservableHabitViewModel: ObservableObject {
    let viewModel: HabitViewModel
    @Published var state: HabitUiState
    private var canceller: (() -> Void)?

    init(viewModel: HabitViewModel = HabitViewModel(repository: HabitViewModel.Companion.shared.defaultRepository)) {
        self.viewModel = viewModel
        self.state = viewModel.uiState.value as! HabitUiState

        self.canceller = viewModel.observeUiState { [weak self] newState in
            DispatchQueue.main.async {
                self?.state = newState
            }
        }
    }

    deinit {
        canceller?()
    }

    func toggleHabit(_ id: String) {
        viewModel.toggleHabit(habitId: id)
    }

    func addHabit(title: String, category: String, icon: String, colorHex: Int64) {
        viewModel.addHabit(title: title, category: category, icon: icon, colorHex: colorHex)
    }

    func deleteHabit(_ id: String) {
        viewModel.deleteHabit(habitId: id)
    }

    func setFilter(_ filter: HabitFilter) {
        viewModel.setFilter(filter: filter)
    }
}

// MARK: - Main ContentView (SwiftUI)
struct ContentView: View {
    @StateObject private var vm = ObservableHabitViewModel()
    @State private var showingAddSheet = false
    @State private var showingReminderSheet = false

    var body: some View {
        NavigationStack {
            List {
                // Section 1: Dashboard Summary
                Section {
                    SummaryCardView(summary: vm.state.summary)
                        .listRowInsets(EdgeInsets())
                        .listRowBackground(Color.clear)
                }

                // Section 2: Segmented Filter
                Section {
                    Picker("Filter", selection: Binding(
                        get: { vm.state.filter },
                        set: { vm.setFilter($0) }
                    )) {
                        Text("All").tag(HabitFilter.all)
                        Text("Pending").tag(HabitFilter.pendingToday)
                        Text("Done").tag(HabitFilter.completedToday)
                    }
                    .pickerStyle(.segmented)
                    .listRowInsets(EdgeInsets(top: 4, leading: 4, bottom: 4, trailing: 4))
                    .listRowBackground(Color.clear)
                }

                // Section 3: Habit Items
                Section(header: Text(sectionHeaderTitle).font(.subheadline).bold()) {
                    if vm.state.items.isEmpty {
                        EmptyStateRow(filter: vm.state.filter)
                    } else {
                        ForEach(vm.state.items, id: \.habit.id) { item in
                            HabitRowView(
                                item: item,
                                onToggle: {
                                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                                    vm.toggleHabit(item.habit.id)
                                }
                            )
                            .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                                Button(role: .destructive) {
                                    vm.deleteHabit(item.habit.id)
                                } label: {
                                    Label("Delete", systemImage: "trash")
                                }
                            }
                        }
                    }
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle("Habits")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button {
                        showingReminderSheet = true
                    } label: {
                        Image(systemName: "bell.badge.fill")
                            .font(.body)
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        showingAddSheet = true
                    } label: {
                        Image(systemName: "plus.circle.fill")
                            .font(.title3)
                    }
                }
            }
            .sheet(isPresented: $showingAddSheet) {
                AddHabitSheet { title, category, icon, colorHex in
                    vm.addHabit(title: title, category: category, icon: icon, colorHex: colorHex)
                }
            }
            .sheet(isPresented: $showingReminderSheet) {
                ReminderSheet(summary: vm.state.summary)
            }
        }
    }

    private var sectionHeaderTitle: String {
        switch vm.state.filter {
        case .all: return "All Habits (\(vm.state.items.count))"
        case .pendingToday: return "Pending Today (\(vm.state.items.count))"
        case .completedToday: return "Completed Today (\(vm.state.items.count))"
        default: return "Habits"
        }
    }
}

// MARK: - Summary Card
struct SummaryCardView: View {
    let summary: HabitSummary

    var body: some View {
        VStack(spacing: 16) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Today's Progress")
                        .font(.headline)
                        .fontWeight(.bold)
                    Text("\(summary.completedTodayCount) of \(summary.totalHabits) habits completed")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                Spacer()
                Text("\(summary.completionPercentage)%")
                    .font(.system(size: 32, weight: .heavy, design: .rounded))
                    .foregroundColor(.accentColor)
            }

            ProgressView(value: Double(summary.completionPercentage), total: 100.0)
                .tint(.accentColor)
                .scaleEffect(x: 1, y: 1.8, anchor: .center)
                .clipShape(Capsule())

            HStack(spacing: 12) {
                StatPill(icon: "trophy.fill", title: "Best Streak", value: "\(summary.bestStreak) days", color: .yellow)
                StatPill(icon: "flame.fill", title: "Active", value: "\(summary.activeStreaksCount)", color: .orange)
            }
        }
        .padding(18)
        .background(Color(UIColor.secondarySystemGroupedBackground))
        .cornerRadius(18)
        .shadow(color: Color.black.opacity(0.04), radius: 8, x: 0, y: 3)
    }
}

struct StatPill: View {
    let icon: String
    let title: String
    let value: String
    let color: Color

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .foregroundColor(color)
                .font(.body)
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.caption2)
                    .foregroundColor(.secondary)
                Text(value)
                    .font(.caption)
                    .fontWeight(.bold)
            }
            Spacer()
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(Color(UIColor.tertiarySystemGroupedBackground))
        .cornerRadius(10)
    }
}

// MARK: - Habit Row View
struct HabitRowView: View {
    let item: HabitItemUi
    let onToggle: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 14) {
                // Emoji Icon
                ZStack {
                    Circle()
                        .fill(colorFromHex(item.habit.colorHex).opacity(0.2))
                        .frame(width: 44, height: 44)
                    Text(item.habit.icon)
                        .font(.title3)
                }

                // Title & Details
                VStack(alignment: .leading, spacing: 4) {
                    Text(item.habit.title)
                        .font(.body)
                        .fontWeight(.semibold)
                        .strikethrough(item.isCompletedToday, color: .secondary)

                    HStack(spacing: 6) {
                        Text(item.habit.category)
                            .font(.caption)
                            .foregroundColor(.secondary)

                        Text("•")
                            .font(.caption)
                            .foregroundColor(.secondary)

                        if item.currentStreak > 0 {
                            HStack(spacing: 2) {
                                Image(systemName: "flame.fill")
                                    .font(.caption2)
                                    .foregroundColor(.orange)
                                Text("\(item.currentStreak)d streak")
                                    .font(.caption)
                                    .fontWeight(.semibold)
                                    .foregroundColor(.orange)
                            }
                        } else {
                            Text("Start streak")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }

                Spacer()

                // Checkmark Button
                Button(action: onToggle) {
                    Image(systemName: item.isCompletedToday ? "checkmark.circle.fill" : "circle")
                        .font(.system(size: 28))
                        .foregroundColor(item.isCompletedToday ? colorFromHex(item.habit.colorHex) : .secondary.opacity(0.4))
                }
                .buttonStyle(.borderless)
            }

            // 7-Day Mini Dots
            HStack {
                Text("Last 7 days")
                    .font(.caption2)
                    .foregroundColor(.secondary)

                Spacer()

                HStack(spacing: 6) {
                    ForEach(Array(item.last7Days.enumerated()), id: \.offset) { _, day in
                        VStack(spacing: 2) {
                            Circle()
                                .fill(day.isCompleted ? colorFromHex(item.habit.colorHex) : Color(UIColor.systemGray5))
                                .frame(width: 18, height: 18)
                                .overlay(
                                    Circle()
                                        .stroke(day.isToday ? Color.accentColor : Color.clear, lineWidth: 1.5)
                                )
                                .overlay(
                                    Group {
                                        if day.isCompleted {
                                            Image(systemName: "checkmark")
                                                .font(.system(size: 8, weight: .bold))
                                                .foregroundColor(.white)
                                        }
                                    }
                                )

                            Text(day.dayLabel)
                                .font(.system(size: 8))
                                .foregroundColor(day.isToday ? .accentColor : .secondary)
                                .fontWeight(day.isToday ? .bold : .regular)
                        }
                    }
                }
            }
            .padding(.top, 2)
        }
        .padding(.vertical, 4)
    }

    private func colorFromHex(_ hex: Int64) -> Color {
        let r = Double((hex >> 16) & 0xFF) / 255.0
        let g = Double((hex >> 8) & 0xFF) / 255.0
        let b = Double(hex & 0xFF) / 255.0
        return Color(red: r, green: g, blue: b)
    }
}

// MARK: - Empty State
struct EmptyStateRow: View {
    let filter: HabitFilter

    var body: some View {
        HStack {
            Spacer()
            VStack(spacing: 8) {
                Text("🌱")
                    .font(.system(size: 40))
                Text(message)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }
            .padding(.vertical, 24)
            Spacer()
        }
    }

    private var message: String {
        switch filter {
        case .all: return "No habits found. Tap '+' to create one!"
        case .pendingToday: return "All habits completed today. Great job!"
        case .completedToday: return "No habits completed yet today."
        default: return "No habits"
        }
    }
}

// MARK: - Add Habit Sheet
struct AddHabitSheet: View {
    @Environment(\.dismiss) var dismiss
    let onSave: (String, String, String, Int64) -> Void

    @State private var title = ""
    @State private var selectedCategory = "Health"
    @State private var selectedEmoji = "🏃‍♂️"
    @State private var selectedColorHex: Int64 = 0xFFFF6B6B

    let categories = ["Health", "Fitness", "Mind", "Productivity", "General"]
    let emojis = ["🏃‍♂️", "💧", "📚", "🧘", "💻", "🍎", "💤", "🎯", "🎨", "🚴"]
    let colors: [Int64] = [0xFFFF6B6B, 0xFF4ECDC4, 0xFF5C6BC0, 0xFF26A69A, 0xFFFFA07A, 0xFFAB47BC]

    var body: some View {
        NavigationStack {
            Form {
                Section("Habit Title") {
                    TextField("e.g. Read 20 pages", text: $title)
                }

                Section("Category") {
                    Picker("Category", selection: $selectedCategory) {
                        ForEach(categories, id: \.self) { cat in
                            Text(cat).tag(cat)
                        }
                    }
                    .pickerStyle(.menu)
                }

                Section("Icon") {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            ForEach(emojis, id: \.self) { emoji in
                                Text(emoji)
                                    .font(.title2)
                                    .padding(8)
                                    .background(selectedEmoji == emoji ? Color.accentColor.opacity(0.2) : Color.clear)
                                    .clipShape(Circle())
                                    .overlay(
                                        Circle().stroke(selectedEmoji == emoji ? Color.accentColor : Color.clear, lineWidth: 2)
                                    )
                                    .onTapGesture {
                                        selectedEmoji = emoji
                                    }
                            }
                        }
                        .padding(.vertical, 4)
                    }
                }

                Section("Theme Color") {
                    HStack(spacing: 14) {
                        ForEach(colors, id: \.self) { hex in
                            Circle()
                                .fill(colorFromHex(hex))
                                .frame(width: 32, height: 32)
                                .overlay(
                                    Circle().stroke(selectedColorHex == hex ? Color.primary : Color.clear, lineWidth: 3)
                                )
                                .onTapGesture {
                                    selectedColorHex = hex
                                }
                        }
                    }
                    .padding(.vertical, 4)
                }
            }
            .navigationTitle("New Habit")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Add") {
                        if !title.trimmingCharacters(in: .whitespaces).isEmpty {
                            onSave(title, selectedCategory, selectedEmoji, selectedColorHex)
                            dismiss()
                        }
                    }
                    .disabled(title.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
    }

    private func colorFromHex(_ hex: Int64) -> Color {
        let r = Double((hex >> 16) & 0xFF) / 255.0
        let g = Double((hex >> 8) & 0xFF) / 255.0
        let b = Double(hex & 0xFF) / 255.0
        return Color(red: r, green: g, blue: b)
    }
}

// MARK: - Notification Manager
class NotificationManager: ObservableObject {
    static let shared = NotificationManager()

    @Published var isPermissionGranted = false

    init() {
        checkPermission()
    }

    func checkPermission(completion: ((Bool) -> Void)? = nil) {
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            DispatchQueue.main.async {
                let granted = settings.authorizationStatus == .authorized || settings.authorizationStatus == .provisional
                self.isPermissionGranted = granted
                completion?(granted)
            }
        }
    }

    func requestAuthorization(completion: @escaping (Bool) -> Void) {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
            DispatchQueue.main.async {
                self.isPermissionGranted = granted
                completion(granted)
            }
        }
    }

    func scheduleDailyReminder(hour: Int, minute: Int, title: String, body: String) {
        let center = UNUserNotificationCenter.current()
        let identifier = "daily_habit_reminder"

        center.removePendingNotificationRequests(withIdentifiers: [identifier])

        var dateComponents = DateComponents()
        dateComponents.hour = hour
        dateComponents.minute = minute

        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = .default

        let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)

        center.add(request) { error in
            if let error = error {
                print("Error scheduling daily reminder: \(error.localizedDescription)")
            }
        }
    }

    func cancelDailyReminder() {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: ["daily_habit_reminder"])
    }

    func sendInstantNotification(title: String, body: String, completion: (() -> Void)? = nil) {
        requestAuthorization { granted in
            guard granted else { return }

            let content = UNMutableNotificationContent()
            content.title = title
            content.body = body
            content.sound = .default

            let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1.0, repeats: false)
            let request = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: trigger)

            UNUserNotificationCenter.current().add(request) { _ in
                DispatchQueue.main.async {
                    completion?()
                }
            }
        }
    }
}

// MARK: - Reminder Sheet
struct ReminderSheet: View {
    @Environment(\.dismiss) var dismiss
    let summary: HabitSummary

    @ObservedObject private var notificationManager = NotificationManager.shared
    @AppStorage("dailyReminderEnabled") private var dailyReminderEnabled = true
    @AppStorage("reminderHour") private var reminderHour = 20
    @AppStorage("reminderMinute") private var reminderMinute = 0

    @State private var reminderDate = Date()
    @State private var showNotificationSentAlert = false
    @State private var alertMessage = ""

    init(summary: HabitSummary) {
        self.summary = summary
        var components = Calendar.current.dateComponents([.year, .month, .day], from: Date())
        let hour = UserDefaults.standard.object(forKey: "reminderHour") as? Int ?? 20
        let minute = UserDefaults.standard.object(forKey: "reminderMinute") as? Int ?? 0
        components.hour = hour
        components.minute = minute
        _reminderDate = State(initialValue: Calendar.current.date(from: components) ?? Date())
    }

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    VStack(alignment: .leading, spacing: 8) {
                        HStack(spacing: 8) {
                            Text("🔔")
                                .font(.title2)
                            Text("Habit Reminders")
                                .font(.headline)
                                .fontWeight(.bold)
                        }
                        Text("Daily reminders help you stay consistent, check off pending habits before the day ends, and protect your hard-earned streaks.")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    .padding(.vertical, 4)
                }

                Section("Daily Schedule") {
                    Toggle("Evening Reminder", isOn: $dailyReminderEnabled)
                        .onChange(of: dailyReminderEnabled) { isEnabled in
                            if isEnabled {
                                scheduleReminder()
                            } else {
                                notificationManager.cancelDailyReminder()
                            }
                        }

                    if dailyReminderEnabled {
                        DatePicker(
                            "Reminder Time",
                            selection: $reminderDate,
                            displayedComponents: .hourAndMinute
                        )
                        .onChange(of: reminderDate) { newDate in
                            let cal = Calendar.current
                            reminderHour = cal.component(.hour, from: newDate)
                            reminderMinute = cal.component(.minute, from: newDate)
                            scheduleReminder()
                        }
                    }
                }

                Section("Test Delivery") {
                    Button(action: sendTestNotification) {
                        HStack {
                            Text("Send Test Notification 🚀")
                                .fontWeight(.semibold)
                            Spacer()
                            Image(systemName: "paperplane.fill")
                                .font(.subheadline)
                        }
                    }

                    Text("Tapping this button triggers an immediate notification so you can verify that system alerts and banner notifications are functioning properly.")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            .navigationTitle("Reminders & Alerts")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") {
                        dismiss()
                    }
                    .fontWeight(.bold)
                }
            }
            .alert("Notification Scheduled!", isPresented: $showNotificationSentAlert) {
                Button("OK", role: .cancel) { }
            } message: {
                Text(alertMessage)
            }
        }
    }

    private func scheduleReminder() {
        notificationManager.requestAuthorization { granted in
            if granted {
                let cal = Calendar.current
                let hour = cal.component(.hour, from: reminderDate)
                let minute = cal.component(.minute, from: reminderDate)
                notificationManager.scheduleDailyReminder(
                    hour: hour,
                    minute: minute,
                    title: "🔥 HabitStreak Check-in",
                    body: "Time to review your habits today and keep your streak alive!"
                )
            }
        }
    }

    private func sendTestNotification() {
        let pending = summary.totalHabits - summary.completedTodayCount
        let message = pending > 0
            ? "🔥 Protect your streak! You have \(pending) habit\(pending > 1 ? "s" : "") left to complete today."
            : "🎉 All habits checked off today! Your streaks are safe and sound."

        alertMessage = "Notification dispatched! Check your notification banner above."
        UINotificationFeedbackGenerator().notificationOccurred(.success)

        notificationManager.sendInstantNotification(
            title: "🔥 HabitStreak Alert",
            body: message
        ) {
            showNotificationSentAlert = true
        }
    }
}