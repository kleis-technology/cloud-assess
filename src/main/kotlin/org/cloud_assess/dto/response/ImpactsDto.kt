package org.cloud_assess.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class ImpactsDto(
    @get:JsonProperty("ADPe") val ADPe: ImpactDto,
    @get:JsonProperty("ADPf") val ADPf: ImpactDto,
    @get:JsonProperty("AP") val AP: ImpactDto,
    @get:JsonProperty("GWP") val GWP: ImpactDto,
    @get:JsonProperty("LU") val LU: ImpactDto,
    @get:JsonProperty("ODP") val ODP: ImpactDto,
    @get:JsonProperty("PM") val PM: ImpactDto,
    @get:JsonProperty("POCP") val POCP: ImpactDto,
    @get:JsonProperty("WU") val WU: ImpactDto,
    @get:JsonProperty("CTUe") val CTUe: ImpactDto,
    @get:JsonProperty("CTUh_c") val CTUh_c: ImpactDto,
    @get:JsonProperty("CTUh_nc") val CTUh_nc: ImpactDto,
    @get:JsonProperty("Epf") val Epf: ImpactDto,
    @get:JsonProperty("Epm") val Epm: ImpactDto,
    @get:JsonProperty("Ept") val Ept: ImpactDto,
    @get:JsonProperty("IR") val IR: ImpactDto,
)

enum class Indicator {
    ADPe,
    ADPf,
    AP,
    GWP,
    LU,
    ODP,
    PM,
    POCP,
    WU,
    CTUe,
    CTUh_c,
    CTUh_nc,
    Epf,
    Epm,
    Ept,
    IR,
}
