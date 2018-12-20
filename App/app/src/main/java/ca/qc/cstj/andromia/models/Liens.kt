package ca.qc.cstj.andromia.models

import kotlinx.serialization.Serializable

@Serializable
data class Liens(
        val self: LienPage,
        val prev: LienPage,
        val next: LienPage,
        val first: LienPage,
        val last: LienPage)