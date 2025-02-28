package com.wlvpn.consumervpn.data.repository.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.wlvpn.consumervpn.data.OpenVpnSettingsProto
import java.io.InputStream
import java.io.OutputStream

object OpenVpnSettingsProtoSerializer : Serializer<OpenVpnSettingsProto> {
    override val defaultValue: OpenVpnSettingsProto = OpenVpnSettingsProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): OpenVpnSettingsProto {
        try {
            return OpenVpnSettingsProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: OpenVpnSettingsProto,
        output: OutputStream
    ) = t.writeTo(output)
}