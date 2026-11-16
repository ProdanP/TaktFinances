package com.prodan.taktfinances.accounts.domain.entity

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** Today's date in the device's time zone. Rules take the date as a parameter; only this reads the clock. */
fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
