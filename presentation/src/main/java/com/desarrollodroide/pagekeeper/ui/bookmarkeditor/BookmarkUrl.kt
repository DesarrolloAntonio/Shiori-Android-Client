package com.desarrollodroide.pagekeeper.ui.bookmarkeditor

/**
 * Turns what someone typed into something the server will accept.
 *
 * Typing `google.es` and pressing add produced a 502 with an empty body, so the app threw
 * "Error adding bookmark: " with nothing after the colon, the worker retried it five times and the
 * bookmark sat in the feed as pending for ever. Shiori goes and fetches the page, and it cannot
 * fetch a string with no scheme. `https://google.es` returns 200 for the same input.
 *
 * The keyboard made it worse: the field had no options set, so it auto-capitalised and actually
 * sent `Google.es`.
 */
fun normalizeBookmarkUrl(input: String): String {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return ""
    // Anything that already names a scheme is left alone, including the ones this app will not
    // fetch: rewriting ftp:// or file:// to https:// would be a stranger failure than passing it
    // through and letting the server say no.
    return if (SCHEME.containsMatchIn(trimmed)) trimmed else "https://$trimmed"
}

/**
 * Whether this is worth sending at all.
 *
 * Deliberately shallow. The point is to catch the empty box and the pasted paragraph, not to
 * adjudicate what a valid url is; the server is the authority on that.
 */
fun isPlausibleBookmarkUrl(input: String): Boolean {
    val normalized = normalizeBookmarkUrl(input)
    if (normalized.isEmpty()) return false
    if (normalized.any { it.isWhitespace() }) return false
    val host = normalized
        .substringAfter("://", "")
        .substringBefore('/')
        .substringBefore('?')
    return host.isNotEmpty() && host.contains('.') && !host.startsWith('.') && !host.endsWith('.')
}

private val SCHEME = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://")
