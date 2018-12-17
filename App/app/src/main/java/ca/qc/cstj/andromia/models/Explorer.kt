package ca.qc.cstj.andromia.models

import kotlinx.serialization.Serializable

@Serializable
data class Explorer(
        val runes: Runes,
        val units: List<Unit>,
        val inox: Inox,
        val explorations: List<Exploration>,
        val username: String,
        val email: String,
        val href: String) {

}