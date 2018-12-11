package ca.qc.cstj.andromia.serializers

import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import java.text.SimpleDateFormat
import java.util.*

@Serializer(forClass = Date::class)
object DateSerializer: KSerializer<Date> {
    private val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    override val descriptor: SerialDescriptor = SerialClassDescImpl("Date")

    override fun serialize(output: Encoder, obj: Date) {
        output.encodeString(dateFormat.format(obj))
    }

    override fun deserialize(input: Decoder): Date {
        return dateFormat.parse(input.decodeString())
    }

}