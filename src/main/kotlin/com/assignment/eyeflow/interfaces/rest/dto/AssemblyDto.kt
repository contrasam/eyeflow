package com.assignment.eyeflow.interfaces.rest.dto

import com.assignment.eyeflow.domain.model.assembly.Assembly
import com.assignment.eyeflow.domain.model.assembly.AssemblyComponent
import com.assignment.eyeflow.domain.model.assembly.ComponentType
import java.time.LocalDateTime
import java.util.UUID

/**
 * Data Transfer Object for creating a new assembly.
 */
data class CreateAssemblyRequest(
    val orderId: UUID,
    val components: List<AssemblyComponentRequest>
)

/**
 * Data Transfer Object for assembly component in a request.
 */
data class AssemblyComponentRequest(
    val id: String,
    val type: String,
    val description: String
)

/**
 * Data Transfer Object for assembly response.
 */
data class AssemblyResponse(
    val id: UUID,
    val orderId: UUID,
    val status: String,
    val components: List<AssemblyComponentResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun fromDomain(assembly: Assembly): AssemblyResponse {
            return AssemblyResponse(
                id = assembly.id.value,
                orderId = assembly.orderId.value,
                status = assembly.status.name,
                components = assembly.components.map { AssemblyComponentResponse.fromDomain(it) },
                createdAt = assembly.createdAt,
                updatedAt = assembly.updatedAt
            )
        }
    }
}

/**
 * Data Transfer Object for assembly component in a response.
 */
data class AssemblyComponentResponse(
    val id: String,
    val type: String,
    val description: String,
    val acquired: Boolean
) {
    companion object {
        fun fromDomain(component: AssemblyComponent): AssemblyComponentResponse {
            return AssemblyComponentResponse(
                id = component.id,
                type = component.type.name,
                description = component.description,
                acquired = component.acquired
            )
        }
    }
}
