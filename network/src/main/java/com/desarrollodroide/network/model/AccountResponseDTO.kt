package com.desarrollodroide.network.model

/** `GET /api/v1/auth/me`, wrapped by Shiori 1.8 as `{"ok": bool, "message": account}`. */
data class AccountResponseDTO(
    val ok: Boolean?,
    val message: AccountDTO?,
)
