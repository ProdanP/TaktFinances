package com.prodan.taktfinances

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform