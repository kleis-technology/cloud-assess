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
import type { PoolAssessmentDto } from './PoolAssessmentDto';
import {
    PoolAssessmentDtoFromJSON,
    PoolAssessmentDtoFromJSONTyped,
    PoolAssessmentDtoToJSON,
} from './PoolAssessmentDto';

/**
 * 
 * @export
 * @interface PoolListAssessmentDto
 */
export interface PoolListAssessmentDto {
    /**
     * 
     * @type {Array<PoolAssessmentDto>}
     * @memberof PoolListAssessmentDto
     */
    pools: Array<PoolAssessmentDto>;
}

/**
 * Check if a given object implements the PoolListAssessmentDto interface.
 */
export function instanceOfPoolListAssessmentDto(value: object): value is PoolListAssessmentDto {
    if (!('pools' in value) || value['pools'] === undefined) return false;
    return true;
}

export function PoolListAssessmentDtoFromJSON(json: any): PoolListAssessmentDto {
    return PoolListAssessmentDtoFromJSONTyped(json, false);
}

export function PoolListAssessmentDtoFromJSONTyped(json: any, ignoreDiscriminator: boolean): PoolListAssessmentDto {
    if (json == null) {
        return json;
    }
    return {
        
        'pools': ((json['pools'] as Array<any>).map(PoolAssessmentDtoFromJSON)),
    };
}

export function PoolListAssessmentDtoToJSON(value?: PoolListAssessmentDto | null): any {
    if (value == null) {
        return value;
    }
    return {
        
        'pools': ((value['pools'] as Array<any>).map(PoolAssessmentDtoToJSON)),
    };
}

