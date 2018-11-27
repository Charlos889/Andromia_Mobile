package ca.qc.cstj.andromia.models

import java.util.*

data class Unit(val name : String,
                val life : Int,
                val speed : Int,
                val imageURL : String,
                val affinity : String,
                val runes : UnitRunes,
                val set : String,
                val uuid : String,
                val kernel : Runes,
                val createdDate : Date) { }