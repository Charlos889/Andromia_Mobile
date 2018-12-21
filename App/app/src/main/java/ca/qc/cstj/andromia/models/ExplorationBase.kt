package ca.qc.cstj.andromia.models

import ca.qc.cstj.andromia.serializers.DateSerializer
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class ExplorationBase(
        val destination: String,
        val runes: Runes,
        val unit: Unit,
        @Serializable(with = DateSerializer::class) val dateExploration: Date,
        @Optional var capture : Boolean? = null) {
}