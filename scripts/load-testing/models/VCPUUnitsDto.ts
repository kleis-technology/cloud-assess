/* tslint:disable */
/* eslint-disable */
/**
 * Cloud Assess
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 1.6.5
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


/**
 * 
 * @export
 */
export const VCPUUnitsDto = {
    VCpu: 'vCPU'
} as const;
export type VCPUUnitsDto = typeof VCPUUnitsDto[keyof typeof VCPUUnitsDto];


export function instanceOfVCPUUnitsDto(value: any): boolean {
    for (const key in VCPUUnitsDto) {
        if (Object.prototype.hasOwnProperty.call(VCPUUnitsDto, key)) {
            if (VCPUUnitsDto[key] === value) {
                return true;
            }
        }
    }
    return false;
}

export function VCPUUnitsDtoFromJSON(json: any): VCPUUnitsDto {
    return VCPUUnitsDtoFromJSONTyped(json, false);
}

export function VCPUUnitsDtoFromJSONTyped(json: any, ignoreDiscriminator: boolean): VCPUUnitsDto {
    return json as VCPUUnitsDto;
}

export function VCPUUnitsDtoToJSON(value?: VCPUUnitsDto | null): any {
    return value as any;
}

