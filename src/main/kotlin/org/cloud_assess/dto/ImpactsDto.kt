package org.cloud_assess.dto

data class ImpactsDto(
    val ADPe: ImpactDto,
    val ADPf: ImpactDto,
    val AP: ImpactDto,
    val GWP: ImpactDto,
    val LU: ImpactDto,
    val ODP: ImpactDto,
    val PM: ImpactDto,
    val POCP: ImpactDto,
    val WU: ImpactDto,
    val CTUe: ImpactDto,
    val CTUh_c: ImpactDto,
    val CTUh_nc: ImpactDto,
    val Epf: ImpactDto,
    val Epm: ImpactDto,
    val Ept: ImpactDto,
    val IR: ImpactDto,
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
