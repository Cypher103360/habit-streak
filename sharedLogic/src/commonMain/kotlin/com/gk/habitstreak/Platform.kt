package com.gk.habitstreak

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform