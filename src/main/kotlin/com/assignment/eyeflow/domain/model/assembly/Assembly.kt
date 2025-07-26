package com.assignment.eyeflow.domain.model.assembly

import com.assignment.eyeflow.domain.model.order.OrderId
import java.time.LocalDateTime
import java.util.UUID

/**
 * Assembly aggregate root representing the assembly process of an eyeglass order.
 */
class Assembly private constructor(
    val id: AssemblyId,
    val orderId: OrderId,
    var status: AssemblyStatus,
    val components: MutableList<AssemblyComponent>,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
) {
    companion object {
        fun create(orderId: OrderId, components: List<AssemblyComponent>): Assembly {
            val now = LocalDateTime.now()
            return Assembly(
                id = AssemblyId(UUID.randomUUID()),
                orderId = orderId,
                status = AssemblyStatus.PENDING,
                components = components.toMutableList(),
                createdAt = now,
                updatedAt = now
            )
        }
    }

    fun startAssembly() {
        require(status == AssemblyStatus.PENDING) { "Assembly can only be started when in PENDING status" }
        status = AssemblyStatus.IN_PROGRESS
        updatedAt = LocalDateTime.now()
    }

    fun completeAssembly() {
        require(status == AssemblyStatus.IN_PROGRESS) { "Assembly can only be completed when in IN_PROGRESS status" }
        require(components.all { it.acquired }) { "All components must be acquired before completing assembly" }
        status = AssemblyStatus.COMPLETED
        updatedAt = LocalDateTime.now()
    }

    fun acquireComponent(componentId: String) {
        val component = components.find { it.id == componentId }
            ?: throw IllegalArgumentException("Component with ID $componentId not found")
        component.acquired = true
        updatedAt = LocalDateTime.now()
    }
}

data class AssemblyId(val value: UUID) {
    override fun toString(): String = value.toString()
}

data class AssemblyComponent(
    val id: String,
    val type: ComponentType,
    val description: String,
    var acquired: Boolean = false
)

enum class ComponentType {
    FRAME,
    LENS
}

enum class AssemblyStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
