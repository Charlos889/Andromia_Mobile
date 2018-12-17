package ca.qc.cstj.andromia.models

import kotlinx.serialization.Serializable

@Serializable
data class PositionExploration(
        val coordonnees: Position,
        val nom: String) {
}