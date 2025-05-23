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
import type { PoolDto } from './PoolDto';
import {
    PoolDtoFromJSON,
    PoolDtoFromJSONTyped,
    PoolDtoToJSON,
} from './PoolDto';
import type { QuantityTimeDto } from './QuantityTimeDto';
import {
    QuantityTimeDtoFromJSON,
    QuantityTimeDtoFromJSONTyped,
    QuantityTimeDtoToJSON,
} from './QuantityTimeDto';
import type { ImpactsDto } from './ImpactsDto';
import {
    ImpactsDtoFromJSON,
    ImpactsDtoFromJSONTyped,
    ImpactsDtoToJSON,
} from './ImpactsDto';

/**
 * 
 * @export
 * @interface PoolAssessmentDto
 */
export interface PoolAssessmentDto {
    /**
     * 
     * @type {QuantityTimeDto}
     * @memberof PoolAssessmentDto
     */
    period: QuantityTimeDto;
    /**
     * 
     * @type {PoolDto}
     * @memberof PoolAssessmentDto
     */
    request: PoolDto;
    /**
     * 
     * @type {ImpactsDto}
     * @memberof PoolAssessmentDto
     */
    impacts: ImpactsDto;
}

/**
 * Check if a given object implements the PoolAssessmentDto interface.
 */
export function instanceOfPoolAssessmentDto(value: object): value is PoolAssessmentDto {
    if (!('period' in value) || value['period'] === undefined) return false;
    if (!('request' in value) || value['request'] === undefined) return false;
    if (!('impacts' in value) || value['impacts'] === undefined) return false;
    return true;
}

export function PoolAssessmentDtoFromJSON(json: any): PoolAssessmentDto {
    return PoolAssessmentDtoFromJSONTyped(json, false);
}

export function PoolAssessmentDtoFromJSONTyped(json: any, ignoreDiscriminator: boolean): PoolAssessmentDto {
    if (json == null) {
        return json;
    }
    return {
        
        'period': QuantityTimeDtoFromJSON(json['period']),
        'request': PoolDtoFromJSON(json['request']),
        'impacts': ImpactsDtoFromJSON(json['impacts']),
    };
}

export function PoolAssessmentDtoToJSON(value?: PoolAssessmentDto | null): any {
    if (value == null) {
        return value;
    }
    return {
        
        'period': QuantityTimeDtoToJSON(value['period']),
        'request': PoolDtoToJSON(value['request']),
        'impacts': ImpactsDtoToJSON(value['impacts']),
    };
}

