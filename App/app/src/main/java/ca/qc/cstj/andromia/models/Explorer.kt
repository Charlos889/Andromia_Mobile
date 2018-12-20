package ca.qc.cstj.andromia.models

import android.databinding.BaseObservable
import kotlinx.serialization.Serializable

@Serializable
data class Explorer constructor(
        val runes: Runes,
        val units: LienPage,
        val inox: Inox,
        val explorations: LienPage,
        val username: String,
        val email: String,
        val href: String) : BaseObservable() {

    constructor() : this(Runes(), LienPage(""), Inox(0), LienPage(""), "Andromia", "", "")
}