package com.desarrollodroide.domain.usecase

import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.repository.AuthRepository
import com.desarrollodroide.data.repository.SessionCheck
import com.desarrollodroide.data.repository.SyncWorks
import kotlinx.coroutines.flow.first

/**
 * Moves the signed-in session to a new address of the same server (issue #64).
 *
 * Logging out was the only way to change the address, and logging out drops every change still
 * waiting to upload: someone whose server moved lost a month of shared links that way. Only the
 * address changes here; the session, the cached bookmarks and the queue stay.
 */
class ChangeServerUrlUseCase(
    private val authRepository: AuthRepository,
    private val settings: SettingsPreferenceDataSource,
    private val syncWorks: SyncWorks,
) {
    suspend operator fun invoke(newUrl: String): SessionCheck {
        val url = newUrl.trim().trimEnd('/')
        val user = settings.getUser().first()
        val check = authRepository.checkSession(url, user.token, user.account.userName)
        if (check == SessionCheck.Accepted) {
            settings.updateServerUrl(url)
            // Jobs that kept failing against the old address may have given up for good, and
            // those never run again on their own.
            syncWorks.retryAllPendingJobs()
        }
        return check
    }
}
