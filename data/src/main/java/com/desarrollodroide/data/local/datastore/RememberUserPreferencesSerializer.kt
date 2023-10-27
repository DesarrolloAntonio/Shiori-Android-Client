package com.desarrollodroide.data.local.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.desarrollodroide.data.RememberUserPreferences
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

/**
 * Serializer for the [RememberUserPreferences] object defined in user_prefs.proto.
 */
object RememberUserPreferencesSerializer : Serializer<RememberUserPreferences> {
    override val defaultValue: RememberUserPreferences = RememberUserPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): RememberUserPreferences {
        try {
            return RememberUserPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: RememberUserPreferences, output: OutputStream) = t.writeTo(output)
}

