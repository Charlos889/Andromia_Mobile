package ca.qc.cstj.andromia.models

import ca.qc.cstj.andromia.serializers.DateSerializer
import java.util.*
import kotlinx.serialization.*
import kotlinx.serialization.Optional

@Serializable
data class Unit(@Optional val name : String? = null,
                @Optional val life : Int? = null,
                @Optional val speed : Int? = null,
                @Optional val imageURL : String? = null,
                @Optional val affinity : String? = null,
                @Optional val runes : UnitRunes? = null,
                @Optional val set : String? = null,
                @Optional val number : Int? = null,
                @Optional val uuid : String? = null,
                @Optional val kernel : Runes? = null,
                @Optional @Serializable(with = DateSerializer::class) val createdDate : Date? = null) {}


