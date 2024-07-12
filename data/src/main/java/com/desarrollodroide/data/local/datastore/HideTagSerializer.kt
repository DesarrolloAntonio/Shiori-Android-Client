package com.desarrollodroide.data.local.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.desarrollodroide.data.HideTag
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

/**
 * Serializer for the [HideTag] object defined in your .proto file.
 */
object HideTagSerializer : Serializer<HideTag> {
    override val defaultValue: HideTag = HideTag.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): HideTag {
        try {
            return HideTag.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: HideTag, output: OutputStream) = t.writeTo(output)
}