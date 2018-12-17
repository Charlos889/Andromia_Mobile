package ca.qc.cstj.andromia.models
import kotlinx.serialization.*

@Serializable
data class UnitRunes(val weapons : List<String>, val abilities : List<String>) {
    constructor() : this(arrayListOf(), arrayListOf())
}