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
import type { QuantityDimensionlessDto } from './QuantityDimensionlessDto';
import {
    QuantityDimensionlessDtoFromJSON,
    QuantityDimensionlessDtoFromJSONTyped,
    QuantityDimensionlessDtoToJSON,
} from './QuantityDimensionlessDto';

/**
 * 
 * @export
 * @interface PoolDto
 */
export interface PoolDto {
    /**
     * 
     * @type {string}
     * @memberof PoolDto
     */
    id: string;
    /**
     * 
     * @type {QuantityDimensionlessDto}
     * @memberof PoolDto
     */
    serviceLevel: QuantityDimensionlessDto;
    /**
     * 
     * @type {{ [key: string]: string; }}
     * @memberof PoolDto
     */
    meta: { [key: string]: string; };
}

/**
 * Check if a given object implements the PoolDto interface.
 */
export function instanceOfPoolDto(value: object): value is PoolDto {
    if (!('id' in value) || value['id'] === undefined) return false;
    if (!('serviceLevel' in value) || value['serviceLevel'] === undefined) return false;
    if (!('meta' in value) || value['meta'] === undefined) return false;
    return true;
}

export function PoolDtoFromJSON(json: any): PoolDto {
    return PoolDtoFromJSONTyped(json, false);
}

export function PoolDtoFromJSONTyped(json: any, ignoreDiscriminator: boolean): PoolDto {
    if (json == null) {
        return json;
    }
    return {
        
        'id': json['id'],
        'serviceLevel': QuantityDimensionlessDtoFromJSON(json['service_level']),
        'meta': json['meta'],
    };
}

export function PoolDtoToJSON(value?: PoolDto | null): any {
    if (value == null) {
        return value;
    }
    return {
        
        'id': value['id'],
        'service_level': QuantityDimensionlessDtoToJSON(value['serviceLevel']),
        'meta': value['meta'],
    };
}

