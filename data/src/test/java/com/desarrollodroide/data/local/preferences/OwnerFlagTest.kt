package com.desarrollodroide.data.local.preferences

import androidx.datastore.core.DataStore
import com.desarrollodroide.data.UserPreferences
import com.desarrollodroide.data.copy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

/**
 * The stored account says whether it is the server's owner.
 *
 * `saveUser` copied id, username, session, url and token but never `owner`, so a real login always
 * stored false, and a logout left behind whatever an earlier write had put there (read from the
 * store on a device right after logout: session empty, owner still true).
 */
class OwnerFlagTest {

    private class InMemoryDataStore(initial: UserPreferences) : DataStore<UserPreferences> {
        private val state = MutableStateFlow(initial)
        override val data: Flow<UserPreferences> = state
        override suspend fun updateData(transform: suspend (t: UserPreferences) -> UserPreferences): UserPreferences =
            transform(state.value).also { state.value = it }
    }

    private fun source(store: DataStore<UserPreferences>) = SettingsPreferencesDataSourceImpl(
        dataStore = mock(),
        protoDataStore = store,
        rememberUserProtoDataStore = mock(),
        systemPreferences = mock(),
        hideTagDataStore = mock(),
    )

    private fun session(owner: Boolean) = UserPreferences.getDefaultInstance().copy {
        id = 1
        username = "qa"
        session = "token"
        token = "token"
        this.owner = owner
    }

    @Test
    fun `logging in as the owner stores owner`() = runTest {
        val store = InMemoryDataStore(UserPreferences.getDefaultInstance())

        source(store).saveUser(session = session(owner = true), serverUrl = "http://s", password = "")

        assertEquals(true, store.data.first().owner)
    }

    /** The pair (R7): a session that is not the owner's stores false over an earlier true. */
    @Test
    fun `a session that is not the owner's clears an earlier owner`() = runTest {
        val store = InMemoryDataStore(session(owner = true))

        source(store).saveUser(session = session(owner = false), serverUrl = "http://s", password = "")

        assertEquals(false, store.data.first().owner)
    }
}
