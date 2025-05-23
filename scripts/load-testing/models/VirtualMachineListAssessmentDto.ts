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

import { mapValues } from '../runtime';
import type { VirtualMachineAssessmentDto } from './VirtualMachineAssessmentDto';
import {
    VirtualMachineAssessmentDtoFromJSON,
    VirtualMachineAssessmentDtoFromJSONTyped,
    VirtualMachineAssessmentDtoToJSON,
} from './VirtualMachineAssessmentDto';

/**
 * 
 * @export
 * @interface VirtualMachineListAssessmentDto
 */
export interface VirtualMachineListAssessmentDto {
    /**
     * 
     * @type {Array<VirtualMachineAssessmentDto>}
     * @memberof VirtualMachineListAssessmentDto
     */
    virtualMachines: Array<VirtualMachineAssessmentDto>;
}

/**
 * Check if a given object implements the VirtualMachineListAssessmentDto interface.
 */
export function instanceOfVirtualMachineListAssessmentDto(value: object): value is VirtualMachineListAssessmentDto {
    if (!('virtualMachines' in value) || value['virtualMachines'] === undefined) return false;
    return true;
}

export function VirtualMachineListAssessmentDtoFromJSON(json: any): VirtualMachineListAssessmentDto {
    return VirtualMachineListAssessmentDtoFromJSONTyped(json, false);
}

export function VirtualMachineListAssessmentDtoFromJSONTyped(json: any, ignoreDiscriminator: boolean): VirtualMachineListAssessmentDto {
    if (json == null) {
        return json;
    }
    return {
        
        'virtualMachines': ((json['virtual_machines'] as Array<any>).map(VirtualMachineAssessmentDtoFromJSON)),
    };
}

export function VirtualMachineListAssessmentDtoToJSON(value?: VirtualMachineListAssessmentDto | null): any {
    if (value == null) {
        return value;
    }
    return {
        
        'virtual_machines': ((value['virtualMachines'] as Array<any>).map(VirtualMachineAssessmentDtoToJSON)),
    };
}

