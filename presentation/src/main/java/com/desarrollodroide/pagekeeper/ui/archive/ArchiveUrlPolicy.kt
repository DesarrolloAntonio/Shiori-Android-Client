package com.desarrollodroide.pagekeeper.ui.archive

import java.net.URI

/** Where a link inside the archive viewer may go. */
enum class ArchiveNavigation {
    /** A file of the archive being shown: it loads in the viewer. */
    InViewer,

    /** Another site: it opens outside the app, like a link in the reader. */
    External,

    /** Anything else on the user's server, or a scheme with no business here. */
    Blocked,
}

/** The archived page itself. The trailing slash is how Shiori's own archive page asks for it. */
fun archiveFileUrl(serverUrl: String, bookmarkId: Int): String =
    "${serverUrl.trimEnd('/')}/bookmark/$bookmarkId/archive/file/"

/**
 * The viewer holds the session in a cookie, so only [bookmarkId]'s archive may load in it: the API,
 * another bookmark or the login page Shiori redirects a refused token to are all [Blocked].
 */
fun classifyArchiveNavigation(url: String, serverUrl: String, bookmarkId: Int): ArchiveNavigation {
    val target = runCatching { URI(url) }.getOrNull() ?: return ArchiveNavigation.Blocked
    val server = runCatching { URI(serverUrl.trimEnd('/') + "/") }.getOrNull() ?: return ArchiveNavigation.Blocked
    val scheme = target.scheme?.lowercase() ?: return ArchiveNavigation.Blocked

    val sameOrigin = scheme == server.scheme?.lowercase() &&
        target.host.equals(server.host, ignoreCase = true) &&
        target.effectivePort() == server.effectivePort()
    if (!sameOrigin) {
        return if (scheme in EXTERNAL_SCHEMES) ArchiveNavigation.External else ArchiveNavigation.Blocked
    }

    // Encoded dots or slashes could climb out of the archive once the server decodes them, and
    // nothing in an archive needs them.
    val rawPath = target.rawPath.orEmpty()
    if (ENCODED_TRAVERSAL.any { rawPath.contains(it, ignoreCase = true) }) return ArchiveNavigation.Blocked
    val archivePrefix = "${server.rawPath}bookmark/$bookmarkId/archive/file/"
    return if (target.normalize().rawPath.orEmpty().startsWith(archivePrefix)) {
        ArchiveNavigation.InViewer
    } else {
        ArchiveNavigation.Blocked
    }
}

private fun URI.effectivePort(): Int = when {
    port != -1 -> port
    scheme.equals("https", ignoreCase = true) -> 443
    else -> 80
}

private val EXTERNAL_SCHEMES = setOf("http", "https", "mailto", "tel")
private val ENCODED_TRAVERSAL = listOf("%2e", "%2f", "%5c")
