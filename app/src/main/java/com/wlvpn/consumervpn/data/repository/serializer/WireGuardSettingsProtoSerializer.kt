package com.wlvpn.consumervpn.data.repository.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.wlvpn.consumervpn.data.WireGuardSettingsProto
import java.io.InputStream
import java.io.OutputStream

object WireGuardSettingsProtoSerializer : Serializer<WireGuardSettingsProto> {
    override val defaultValue: WireGuardSettingsProto = WireGuardSettingsProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): WireGuardSettingsProto {
        try {
            return WireGuardSettingsProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: WireGuardSettingsProto,
        output: OutputStream
    ) = t.writeTo(output)
}