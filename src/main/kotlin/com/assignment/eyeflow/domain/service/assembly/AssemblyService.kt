package com.assignment.eyeflow.domain.service.assembly

import com.assignment.eyeflow.domain.model.assembly.Assembly
import com.assignment.eyeflow.domain.model.assembly.AssemblyId
import com.assignment.eyeflow.domain.model.assembly.AssemblyComponent
import com.assignment.eyeflow.domain.model.order.OrderId
import reactor.core.publisher.Mono

/**
 * Domain service interface for assembly-related operations.
 */
interface AssemblyService {
    /**
     * Creates a new assembly for an order.
     */
    fun createAssembly(orderId: OrderId, components: List<AssemblyComponent>): Mono<Assembly>
    
    /**
     * Starts the assembly process.
     */
    fun startAssembly(assemblyId: AssemblyId): Mono<Assembly>
    
    /**
     * Completes the assembly process.
     */
    fun completeAssembly(assemblyId: AssemblyId): Mono<Assembly>
    
    /**
     * Acquires a component for assembly.
     */
    fun acquireComponent(assemblyId: AssemblyId, componentId: String): Mono<Assembly>
    
    /**
     * Finds an assembly by its ID.
     */
    fun findById(assemblyId: AssemblyId): Mono<Assembly>
    
    /**
     * Finds an assembly by order ID.
     */
    fun findByOrderId(orderId: OrderId): Mono<Assembly>
}
