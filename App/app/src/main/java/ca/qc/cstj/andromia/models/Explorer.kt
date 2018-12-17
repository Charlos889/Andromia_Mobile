package ca.qc.cstj.andromia.models

import android.databinding.BaseObservable
import kotlinx.serialization.Serializable

@Serializable
data class Explorer constructor(
        val runes: Runes,
        val units: List<Unit>,
        val inox: Inox,
        val explorations: List<Exploration>,
        val username: String,
        val email: String,
        val href: String) : BaseObservable() {

    constructor() : this(Runes(), listOf<Unit>(), Inox(0), listOf<Exploration>(), "Andromia", "", "")
}