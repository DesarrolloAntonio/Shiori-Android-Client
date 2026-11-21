package com.desarrollodroide.data.repository.workers

import com.desarrollodroide.data.repository.editedFieldsFromString
import com.desarrollodroide.data.repository.editedFieldsToString
import com.desarrollodroide.model.EditedField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * An edit job carries the fields the user changed in its input and in a tag, and the tag is all
 * that "Retry all" and the next edit can read back. Lost on the way, the upload would quietly send
 * the server's title instead of the user's and still report success.
 */
class EditedFieldsTest {

    @Test
    fun `the edited fields survive the trip through the job`() {
        val fields = setOf(EditedField.TITLE, EditedField.EXCERPT)

        assertEquals(fields, editedFieldsFromString(editedFieldsToString(fields)))
    }

    /** The pair (R7): a job made before this existed, or one that edited nothing, names no field. */
    @Test
    fun `a job without edited fields names none`() {
        assertEquals(emptySet<EditedField>(), editedFieldsFromString(null))
        assertEquals(emptySet<EditedField>(), editedFieldsFromString(editedFieldsToString(emptySet())))
    }

    @Test
    fun `a name this version does not know is ignored`() {
        assertEquals(setOf(EditedField.TITLE), editedFieldsFromString("TITLE,AUTHOR"))
    }
}
