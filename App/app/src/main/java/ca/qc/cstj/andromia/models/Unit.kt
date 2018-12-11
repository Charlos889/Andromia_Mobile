package ca.qc.cstj.andromia.models

import java.util.*
import kotlinx.serialization.*
import java.time.LocalDateTime


@Serializable
data class Unit(val name : String,
                val life : Int,
                val speed : Int,
                val imageURL : String,
                val affinity : String,
                val runes : UnitRunes,
                val set : String,
                val uuid : String,
                val kernel : List<Rune>,
                val createdDate : String)