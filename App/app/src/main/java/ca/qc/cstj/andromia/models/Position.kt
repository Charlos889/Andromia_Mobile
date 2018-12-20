package ca.qc.cstj.andromia.models

import kotlinx.serialization.Serializable

@Serializable
data class Position(
        val x: Int,
        val y: Int)