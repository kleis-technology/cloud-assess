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
import type { MemoryUnitsDto } from './MemoryUnitsDto';
import {
    MemoryUnitsDtoFromJSON,
    MemoryUnitsDtoFromJSONTyped,
    MemoryUnitsDtoToJSON,
} from './MemoryUnitsDto';

/**
 * 
 * @export
 * @interface QuantityMemoryDto
 */
export interface QuantityMemoryDto {
    /**
     * 
     * @type {number}
     * @memberof QuantityMemoryDto
     */
    amount: number;
    /**
     * 
     * @type {MemoryUnitsDto}
     * @memberof QuantityMemoryDto
     */
    unit: MemoryUnitsDto;
}

/**
 * Check if a given object implements the QuantityMemoryDto interface.
 */
export function instanceOfQuantityMemoryDto(value: object): value is QuantityMemoryDto {
    if (!('amount' in value) || value['amount'] === undefined) return false;
    if (!('unit' in value) || value['unit'] === undefined) return false;
    return true;
}

export function QuantityMemoryDtoFromJSON(json: any): QuantityMemoryDto {
    return QuantityMemoryDtoFromJSONTyped(json, false);
}

export function QuantityMemoryDtoFromJSONTyped(json: any, ignoreDiscriminator: boolean): QuantityMemoryDto {
    if (json == null) {
        return json;
    }
    return {
        
        'amount': json['amount'],
        'unit': MemoryUnitsDtoFromJSON(json['unit']),
    };
}

export function QuantityMemoryDtoToJSON(value?: QuantityMemoryDto | null): any {
    if (value == null) {
        return value;
    }
    return {
        
        'amount': value['amount'],
        'unit': MemoryUnitsDtoToJSON(value['unit']),
    };
}

