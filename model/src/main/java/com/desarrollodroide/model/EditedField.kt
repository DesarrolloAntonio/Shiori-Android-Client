package com.desarrollodroide.model

/**
 * A bookmark field the user changed in the editor.
 *
 * An edit is uploaded later, and Shiori's PUT overwrites url, title and excerpt with whatever it is
 * sent. The upload takes those from the server's current copy so it doesn't undo another client's
 * change, except for the fields named here: those are the user's, and they are sent as typed.
 */
enum class EditedField { TITLE, EXCERPT }
