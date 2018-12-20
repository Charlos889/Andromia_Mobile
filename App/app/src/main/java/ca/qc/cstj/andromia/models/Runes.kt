package ca.qc.cstj.andromia.models

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class Runes(@Optional val air : Int = 0,
                 @Optional val darkness : Int = 0,
                 @Optional val earth : Int = 0,
                 @Optional val energy : Int = 0,
                 @Optional val fire : Int = 0,
                 @Optional val life : Int = 0,
                 @Optional val light : Int = 0,
                 @Optional val logic : Int = 0,
                 @Optional val music : Int = 0,
                 @Optional val space : Int = 0,
                 @Optional val toxic : Int = 0,
                 @Optional val water : Int = 0) {

    constructor() : this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
}