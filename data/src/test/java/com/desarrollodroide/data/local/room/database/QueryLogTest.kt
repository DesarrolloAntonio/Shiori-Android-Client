package com.desarrollodroide.data.local.room.database

import androidx.room.RoomDatabase
import java.util.concurrent.Executor
import com.desarrollodroide.data.local.room.database.BookmarksDatabase.Companion.logQueriesIf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/** QA 2026-09-24 LOG-01: the query log printed the whole library to logcat, release included. */
class QueryLogTest {

    @Test
    fun `outside debug the database gets no query callback`() {
        val builder = mock<RoomDatabase.Builder<BookmarksDatabase>>()

        builder.logQueriesIf(enabled = false)

        verify(builder, never()).setQueryCallback(any<RoomDatabase.QueryCallback>(), any<Executor>())
    }

    @Test
    fun `in debug the database logs its queries`() {
        val builder = mock<RoomDatabase.Builder<BookmarksDatabase>>()
        whenever(builder.setQueryCallback(any<RoomDatabase.QueryCallback>(), any<Executor>())).thenReturn(builder)

        builder.logQueriesIf(enabled = true)

        verify(builder).setQueryCallback(any<RoomDatabase.QueryCallback>(), any<Executor>())
    }
}
