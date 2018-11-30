package ca.qc.cstj.andromia.models

import kotlinx.serialization.*

@Serializable
data class Runes(val air : Int,
            val darkness : Int,
            val earth : Int,
            val energy : Int,
            val fire : Int,
            val life : Int,
            val light : Int,
            val logic : Int,
            val music : Int,
            val space : Int,
            val toxic : Int,
            val water : Int) {
}