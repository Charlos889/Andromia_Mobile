package ca.qc.cstj.andromia.models
import android.icu.util.Output
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Serializable
class MonDateTime : Date() {
    @Serializer(forClass = MonDateTime::class)
    object DateSerializer: KSerializer<MonDateTime> {
        override val descriptor: SerialDescriptor =
                StringDescriptor.withName("MonDateTime")

        override fun deserialize(input: Decoder): MonDateTime {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun serialize(output: Encoder, obj: MonDateTime) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        private val df: DateFormat = SimpleDateFormat("yyyy-mm-ddHH:mm:ss.SSS")


    }
}