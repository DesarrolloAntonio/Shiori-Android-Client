package com.desarrollodroide.data.local.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.desarrollodroide.data.SystemPreferences
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

/**
 * Serializer for the [SystemPreferencesSerializer] object defined in user_prefs.proto.
 */
object SystemPreferencesSerializer : Serializer<SystemPreferences> {
    override val defaultValue: SystemPreferences = SystemPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): SystemPreferences {
        try {
            return SystemPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }
    override suspend fun writeTo(t: SystemPreferences, output: OutputStream) = t.writeTo(output)
}

