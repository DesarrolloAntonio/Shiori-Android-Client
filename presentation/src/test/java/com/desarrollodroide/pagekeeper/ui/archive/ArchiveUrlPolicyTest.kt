package com.desarrollodroide.pagekeeper.ui.archive

import com.desarrollodroide.pagekeeper.ui.archive.ArchiveNavigation.Blocked
import com.desarrollodroide.pagekeeper.ui.archive.ArchiveNavigation.External
import com.desarrollodroide.pagekeeper.ui.archive.ArchiveNavigation.InViewer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Issue #66: the viewer holds the session in a cookie, so only this bookmark's archive may load in
 * it. Anything else on the server (the API, another bookmark, the login page a refused token is
 * sent to) would either act as the user or put a web login form inside the app.
 */
class ArchiveUrlPolicyTest {

    private val server = "http://nas.local:18080"

    @Test
    fun `the archive's own files load in the viewer`() {
        assertEquals(InViewer, classifyArchiveNavigation("$server/bookmark/5/archive/file/", server, 5))
        assertEquals(InViewer, classifyArchiveNavigation("$server/bookmark/5/archive/file/css/site.css", server, 5))
    }

    @Test
    fun `a server url with a trailing slash or a sub path still matches`() {
        assertEquals(InViewer, classifyArchiveNavigation("$server/bookmark/5/archive/file/a.png", "$server/", 5))
        assertEquals(
            InViewer,
            classifyArchiveNavigation("https://host.example/shiori/bookmark/5/archive/file/", "https://host.example/shiori", 5),
        )
        assertEquals(
            Blocked,
            classifyArchiveNavigation("https://host.example/bookmark/5/archive/file/", "https://host.example/shiori", 5),
        )
    }

    @Test
    fun `the rest of the server is blocked`() {
        listOf(
            "$server/bookmark/6/archive/file/",
            "$server/login?dst=%2Fbookmark%2F5%2Farchive%2Ffile%2F",
            "$server/api/v1/auth/me",
            "$server/bookmark/5/archive/file/../../../api/v1/auth/me",
            "$server/bookmark/5/archive/file/%2e%2e/%2e%2e/6/archive/file/",
        ).forEach { assertEquals(Blocked, classifyArchiveNavigation(it, server, 5), it) }
    }

    @Test
    fun `other sites open outside the app`() {
        assertEquals(External, classifyArchiveNavigation("https://example.org/page", server, 5))
        assertEquals(External, classifyArchiveNavigation("http://nas.local:9999/bookmark/5/archive/file/", server, 5))
        assertEquals(External, classifyArchiveNavigation("mailto:someone@example.org", server, 5))
    }

    @Test
    fun `schemes that could run or read things are blocked`() {
        listOf("javascript:alert(1)", "file:///data/data/x", "content://x/y", "intent://x#Intent;end", "not a url")
            .forEach { assertEquals(Blocked, classifyArchiveNavigation(it, server, 5), it) }
    }

    @Test
    fun `the archived page is asked for with the trailing slash`() {
        assertEquals("$server/bookmark/5/archive/file/", archiveFileUrl("$server/", 5))
    }
}
