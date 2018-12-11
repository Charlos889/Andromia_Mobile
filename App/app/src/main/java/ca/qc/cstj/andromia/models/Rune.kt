package ca.qc.cstj.andromia.models

import kotlinx.serialization.Serializable

@Serializable
data class Rune(val name : String, val quantity : Int)