package ca.qc.cstj.andromia.models
import kotlinx.serialization.*

@Serializable
data class UnitRunes(val weapons : List<Weapon>, val abilities : List<Ability>) {
}