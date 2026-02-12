package com.desarrollodroide.data.extensions

/**
 * Checks if an integer ID is a temporary timestamp-based ID rather than a real server ID.
 *
 * Temporary IDs are generated from System.currentTimeMillis() / 1000 (epoch seconds),
 * producing values like 1,700,000,000+. Real server IDs are sequential (1, 2, 3...),
 * so any ID over 1 million is clearly a temporary local ID.
 */
fun Int.isTimestampId(): Boolean = this > 1_000_000