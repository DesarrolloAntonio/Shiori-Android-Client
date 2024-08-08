package com.desarrollodroide.pagekeeper.extensions

/**
 * Determines if a string contains more than half Arabic characters.
 *
 * @return True if the string contains more than half Arabic characters, false otherwise.
 */
fun String.isRTLText(): Boolean {
    // Take the first 20 characters of the string
    val textSample = this.take(100)

    // Count the number of Arabic characters in the sample
    val arabicCount = textSample.count { char ->
        // Check if the character is within the Arabic Unicode range
        char in '\u0600'..'\u06FF' ||
                char in '\u0750'..'\u077F' ||
                char in '\u08A0'..'\u08FF' ||
                char in '\uFB50'..'\uFDFF' ||
                char in '\uFE70'..'\uFEFF'
    }

    // Return true if the Arabic character count is greater than half the length of the sample
    return arabicCount > textSample.length / 2
}

