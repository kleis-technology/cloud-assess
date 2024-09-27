package org.cloud_assess.controller

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import org.cloud_assess.api.DebugApi
import org.cloud_assess.dto.ErrorDto
import org.cloud_assess.dto.TraceRequestListDto
import org.cloud_assess.dto.TraceResponseListDto
import org.cloud_assess.service.MapperService
import org.cloud_assess.service.debug.TraceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController

@RestController
class TraceController(
    private val mapperService: MapperService,
    private val traceService: TraceService,
) : DebugApi {
    override fun trace(traceRequestListDto: TraceRequestListDto): ResponseEntity<TraceResponseListDto> {
        val analysis = traceService.analyze(traceRequestListDto)
        val outputDto = mapperService.map(analysis)
        return ResponseEntity.ok(outputDto)
    }

    @ExceptionHandler(value = [IllegalStateException::class, IllegalArgumentException::class, EvaluatorException::class])
    fun handleException(exception: Exception): ResponseEntity<ErrorDto> {
        val errorDto = ErrorDto(
            message = exception.message ?: "unknown"
        )
        val code = when (exception) {
            is IllegalArgumentException -> 400
            is EvaluatorException -> 500
            else -> 500
        }
        return ResponseEntity.status(code).body(errorDto)
    }
}
