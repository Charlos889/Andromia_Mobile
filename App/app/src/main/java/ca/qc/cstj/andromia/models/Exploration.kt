package ca.qc.cstj.andromia.models

import ca.qc.cstj.andromia.serializers.DateSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Exploration(
        val depart: PositionExploration,
        val destination: PositionExploration,
        val runes: Runes,
        val unit: Unit?,
        @Serializable(with = DateSerializer::class) val dateExploration : Date,
        val capture: Boolean,
        val id: String?)