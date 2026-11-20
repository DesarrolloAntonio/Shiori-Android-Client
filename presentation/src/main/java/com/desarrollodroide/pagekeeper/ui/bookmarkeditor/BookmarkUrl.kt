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

/**
 * The title of a link shared from another app.
 *
 * Only EXTRA_TITLE used to be read, but Firefox for Android sends the page title as EXTRA_SUBJECT
 * and no EXTRA_TITLE at all, so everything shared from it was titled with its url. Google News sends
 * neither, but puts the headline in the shared text next to the link, so that text is the next
 * choice. The url still stands in when a share carries no title, so the feed has something to show;
 * it never reaches the server as a title (see toAddBookmarkDTO).
 */
fun sharedBookmarkTitle(
    extraTitle: String?,
    extraSubject: String?,
    url: String,
    sharedText: String = url,
): String {
    val textAroundLink = sharedText.replace(url, " ").replace(WHITESPACE, " ").trim()
    return listOf(extraTitle, extraSubject, textAroundLink).firstOrNull { !it.isNullOrBlank() } ?: url
}

/**
 * The link in a text shared from another app.
 *
 * Most apps share the link alone, but Google News shares "headline + link", and the whole string
 * went to the server as the url (Play review on 1.52.03). The first http(s) link in the text is
 * taken. Text with no such link is passed through for the form and the server to judge, as before.
 */
fun sharedLinkUrl(sharedText: String): String =
    HTTP_LINK.find(sharedText)?.value ?: sharedText.trim()

private val HTTP_LINK = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)
private val WHITESPACE = Regex("""\s+""")

private val SCHEME = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://")
