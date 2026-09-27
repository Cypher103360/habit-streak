package com.gk.habitstreak.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gk.habitstreak.model.DayStatus
import com.gk.habitstreak.model.HabitFilter
import com.gk.habitstreak.model.HabitItemUi
import com.gk.habitstreak.model.HabitSummary
import com.gk.habitstreak.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(
    viewModel: HabitViewModel = remember { HabitViewModel() },
    onTriggerReminder: ((title: String, message: String) -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TODAY'S HABITS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "HabitStreak 🔥",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showReminderDialog = true }) {
                        Text("🔔", fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                icon = {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        val stroke = 2.5.dp.toPx()
                        drawLine(
                            Color.White,
                            Offset(size.width / 2, 0f),
                            Offset(size.width / 2, size.height),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            Color.White,
                            Offset(0f, size.height / 2),
                            Offset(size.width, size.height / 2),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                    }
                },
                text = {
                    Text(
                        text = "New Habit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = 96.dp
            ) // extra bottom padding for FAB
        ) {
            // Hero Progress Dashboard
            item {
                ModernHeroDashboard(state.summary)
            }

            // Modern Segmented Filter
            item {
                SegmentedFilterControl(
                    selectedFilter = state.filter,
                    onFilterSelected = { viewModel.setFilter(it) }
                )
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (state.filter) {
                            HabitFilter.ALL -> "All Habits"
                            HabitFilter.PENDING_TODAY -> "Pending Today"
                            HabitFilter.COMPLETED_TODAY -> "Completed Today"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${state.items.size} total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty State
            if (state.items.isEmpty()) {
                item {
                    ModernEmptyState(state.filter)
                }
            } else {
                items(
                    items = state.items,
                    key = { it.habit.id }
                ) { item ->
                    Box(modifier = Modifier.animateItem()) {
                        ModernHabitCard(
                            item = item,
                            onToggle = { viewModel.toggleHabit(item.habit.id) },
                            onDelete = { viewModel.deleteHabit(item.habit.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, category, icon, colorHex ->
                viewModel.addHabit(title, category, icon, colorHex)
                showAddDialog = false
            }
        )
    }

    if (showReminderDialog) {
        ReminderDialog(
            summary = state.summary,
            onDismiss = { showReminderDialog = false },
            onSendTest = { title, message ->
                onTriggerReminder?.invoke(title, message)
            }
        )
    }
}

@Composable
fun ReminderDialog(
    summary: HabitSummary,
    onDismiss: () -> Unit,
    onSendTest: (title: String, message: String) -> Unit
) {
    var reminderEnabled by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🔔", fontSize = 24.sp)
                Text(
                    "Habit Reminders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Stay consistent! Daily reminders help you check off pending habits and protect your hard-earned streaks.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Evening Reminder",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "8:00 PM • Streak check-in",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it }
                        )
                    }
                }

                Button(
                    onClick = {
                        val pending = summary.totalHabits - summary.completedTodayCount
                        val message = if (pending > 0) {
                            "🔥 Protect your streak! You have $pending habit${if (pending > 1) "s" else ""} left to complete today."
                        } else {
                            "🎉 All habits checked off today! Your streaks are safe and sound."
                        }
                        onSendTest("🔥 HabitStreak Alert", message)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Send Test Notification 🚀", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

// MARK: - Modern Hero Dashboard
@Composable
fun ModernHeroDashboard(summary: HabitSummary) {
    val gradient = remember {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF4F46E5), // Indigo
                Color(0xFF7C3AED)  // Violet
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                6.dp,
                RoundedCornerShape(24.dp),
                ambientColor = Color(0x334F46E5),
                spotColor = Color(0x334F46E5)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Top Progress Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "TODAY'S MOMENTUM",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                        Text(
                            text = "${summary.completedTodayCount} of ${summary.totalHabits} Completed",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    // Percentage Badge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${summary.completionPercentage}%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }

                // Smooth Progress Bar
                val animatedProgress by animateFloatAsState(
                    targetValue = summary.completionPercentage / 100f,
                    label = "progress"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF38BDF8)) // Sky blue glow
                    )
                }

                // Bottom Stat Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FrostedStatPill(
                        icon = "🏆",
                        label = "Best Streak",
                        value = "${summary.bestStreak} days",
                        modifier = Modifier.weight(1f)
                    )
                    FrostedStatPill(
                        icon = "🔥",
                        label = "Active Streaks",
                        value = "${summary.activeStreaksCount}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun FrostedStatPill(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = icon, fontSize = 18.sp)
            Column {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// MARK: - Modern Segmented Filter
@Composable
fun SegmentedFilterControl(
    selectedFilter: HabitFilter,
    onFilterSelected: (HabitFilter) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            HabitFilter.entries.forEach { filter ->
                key(filter) {
                    val label = when (filter) {
                        HabitFilter.ALL -> "All"
                        HabitFilter.PENDING_TODAY -> "Pending"
                        HabitFilter.COMPLETED_TODAY -> "Done"
                    }
                    val isSelected = filter == selectedFilter
                    val tabInteractionSource = remember(filter) { MutableInteractionSource() }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (isSelected) {
                                    Modifier.shadow(
                                        elevation = 2.dp,
                                        shape = RoundedCornerShape(12.dp),
                                        ambientColor = Color(0x15000000),
                                        spotColor = Color(0x15000000)
                                    )
                                } else Modifier
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable(
                                interactionSource = tabInteractionSource,
                                indication = null
                            ) {
                                if (filter != selectedFilter) {
                                    onFilterSelected(filter)
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}


// MARK: - Modern Habit Card
@Composable
fun ModernHabitCard(
    item: HabitItemUi,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val habit = item.habit
    val habitColor = Color(habit.colorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                1.dp,
                RoundedCornerShape(20.dp),
                ambientColor = Color(0x15000000),
                spotColor = Color(0x15000000)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (item.isCompletedToday) habitColor.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Main Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Emoji Icon in rounded rectangle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(habitColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = habit.icon, fontSize = 24.sp)
                }

                // Title & Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Category Pill
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = habit.category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Streak flame pill
                        if (item.currentStreak > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(text = "🔥", fontSize = 12.sp)
                                Text(
                                    text = "${item.currentStreak}d streak",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF97316) // Orange 500
                                )
                            }
                        } else {
                            Text(
                                text = "⚡️ Start streak",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Tactile Completion Checkmark Toggle Button
                val scale by animateFloatAsState(
                    targetValue = if (item.isCompletedToday) 1f else 0.95f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "buttonScale"
                )

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (item.isCompletedToday) habitColor else MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.5f
                            )
                        )
                        .then(
                            if (!item.isCompletedToday) {
                                Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                            } else Modifier
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onToggle()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (item.isCompletedToday) {
                        // Crisp vector checkmark
                        Canvas(modifier = Modifier.size(16.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.15f, size.height * 0.52f)
                                lineTo(size.width * 0.42f, size.height * 0.8f)
                                lineTo(size.width * 0.85f, size.height * 0.25f)
                            }
                            drawPath(
                                path = path,
                                color = Color.White,
                                style = Stroke(
                                    width = 2.5.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            }

            // Divider line
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.75.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )

            // 7-Day Consistency Tracker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LAST 7 DAYS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.last7Days.forEach { day ->
                        ModernDayDot(day = day, habitColor = habitColor)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernDayDot(day: DayStatus, habitColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    if (day.isCompleted) habitColor else MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.6f
                    )
                )
                .then(
                    if (day.isToday) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (day.isCompleted) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.15f, size.height * 0.52f)
                        lineTo(size.width * 0.42f, size.height * 0.8f)
                        lineTo(size.width * 0.85f, size.height * 0.25f)
                    }
                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }

        Text(
            text = day.dayLabel,
            fontSize = 9.sp,
            color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (day.isToday) FontWeight.ExtraBold else FontWeight.Medium
        )
    }
}

// MARK: - Modern Empty State
@Composable
fun ModernEmptyState(filter: HabitFilter) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "🌱", fontSize = 44.sp)
            Text(
                text = when (filter) {
                    HabitFilter.ALL -> "No habits yet. Tap '+ New Habit' to begin your journey!"
                    HabitFilter.PENDING_TODAY -> "Awesome! All habits completed for today."
                    HabitFilter.COMPLETED_TODAY -> "No habits checked off yet today. You've got this!"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// MARK: - Add Habit Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, icon: String, colorHex: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Health") }
    var selectedEmoji by remember { mutableStateOf("🏃‍♂️") }
    var selectedColorHex by remember { mutableStateOf(0xFFFF6B6B) }

    val emojis = listOf("🏃‍♂️", "💧", "📚", "🧘", "💻", "🍎", "💤", "🎯", "🎨", "🚴")
    val colors = listOf(0xFFFF6B6B, 0xFF4ECDC4, 0xFF6366F1, 0xFF10B981, 0xFFF97316, 0xFFA855F7)
    val categories = listOf("Health", "Fitness", "Mind", "Productivity", "General")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Create New Habit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Title") },
                    placeholder = { Text("e.g. Read 20 pages") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Category",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.5f
                                ),
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Emoji picker
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Choose Icon",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(emojis) { emoji ->
                            val isSelected = selectedEmoji == emoji
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.5f
                                        )
                                    )
                                    .then(
                                        if (isSelected) Modifier.border(
                                            2.dp,
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(12.dp)
                                        ) else Modifier
                                    )
                                    .clickable { selectedEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 22.sp)
                            }
                        }
                    }
                }

                // Color picker
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Theme Color",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        colors.forEach { colorLong ->
                            val isSelected = selectedColorHex == colorLong
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorLong))
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                3.dp,
                                                MaterialTheme.colorScheme.onSurface,
                                                CircleShape
                                            )
                                        } else Modifier
                                    )
                                    .clickable { selectedColorHex = colorLong }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, selectedCategory, selectedEmoji, selectedColorHex)
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create Habit", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
