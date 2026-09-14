package com.desarrollodroide.model

import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** How Shiori stores and returns bookmark times: `yyyy-MM-dd HH:mm:ss`, in UTC. */
val SERVER_TIMESTAMP: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

/** "Now" in the server's format, for the times the app writes locally before the server answers. */
fun serverTimestampNow(clock: Clock = Clock.systemUTC()): String =
    LocalDateTime.now(clock.withZone(ZoneOffset.UTC)).format(SERVER_TIMESTAMP)
