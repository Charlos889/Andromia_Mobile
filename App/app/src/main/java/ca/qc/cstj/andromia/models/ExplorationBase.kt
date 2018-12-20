package ca.qc.cstj.andromia.models

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
class ExplorationBase(
        val destination: String,
        val runes: Runes,
        val unit: Unit,
        val dateExploration: String,
        @Optional var capture : Boolean? = null) {
}