package org.cloud_assess.model

data class ResourceTraceAnalysis(
    val meta: Map<String, String>,
    val elements: Map<String, ResourceTrace>,
)
