package com.desarrollodroide.data.extensions

/**
 * Extension function that checks if an integer ID represents a timestamp or not.
 *
 * @return true if the ID is a timestamp (value greater than January 1, 2024), false otherwise
 *
 * This function is used to differentiate between regular IDs and timestamp-based IDs
 * in the data model. Timestamp IDs are considered those with values greater than
 * 1704067200 (2024-01-01 00:00:00 GMT).
 *
 * Usage example:
 * val id = 1704067201
 * if (id.isTimestampId()) {
 *     // Handle timestamp ID
 * } else {
 *     // Handle regular ID
 * }
 */
fun Int.isTimestampId(): Boolean = this > 1704067200  // 2024-01-01 00:00:00 GMT