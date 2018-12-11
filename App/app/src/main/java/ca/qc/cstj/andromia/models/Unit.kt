package ca.qc.cstj.andromia.models

import ca.qc.cstj.andromia.serializers.DateSerializer
import java.util.*
import kotlinx.serialization.*

@Serializable
data class Unit(val name : String,
                val life : Int,
                val speed : Int,
                val imageURL : String,
                val affinity : String,
                val runes : UnitRunes,
                val set : String,
                val uuid : String,
                val kernel : Runes,
                @Serializable(with = DateSerializer::class) val createdDate : Date) {}


