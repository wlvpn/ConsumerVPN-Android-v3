package com.wlvpn.consumervpn.data.repository.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.wlvpn.consumervpn.data.ConnectionSettingsProto
import java.io.InputStream
import java.io.OutputStream

object ConnectionSettingsProtoSerializer : Serializer<ConnectionSettingsProto> {
    override val defaultValue: ConnectionSettingsProto =
        ConnectionSettingsProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ConnectionSettingsProto {
        try {
            return ConnectionSettingsProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: ConnectionSettingsProto,
        output: OutputStream
    ) = t.writeTo(output)
}