package ca.qc.cstj.andromia.models

import kotlinx.serialization.Serializable

@Serializable
data class Exploration(
        val depart: PositionExploration,
        val destination: PositionExploration,
        val runes: Runes,
        val unit: Unit,
        val dateExploration: String,
        val capture: Boolean,
        val id: String?) {
}