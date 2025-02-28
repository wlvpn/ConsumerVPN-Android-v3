package com.wlvpn.consumervpn.data.repository.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.wlvpn.consumervpn.data.IKEv2SettingsProto
import java.io.InputStream
import java.io.OutputStream

object IKEv2SettingsProtoSerializer : Serializer<IKEv2SettingsProto> {
    override val defaultValue: IKEv2SettingsProto = IKEv2SettingsProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): IKEv2SettingsProto {
        try {
            return IKEv2SettingsProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: IKEv2SettingsProto,
        output: OutputStream
    ) = t.writeTo(output)
}