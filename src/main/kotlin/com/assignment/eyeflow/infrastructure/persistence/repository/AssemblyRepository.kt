package com.assignment.eyeflow.infrastructure.persistence.repository

import com.assignment.eyeflow.domain.model.assembly.Assembly
import com.assignment.eyeflow.domain.model.assembly.AssemblyId
import com.assignment.eyeflow.domain.model.order.OrderId
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

/**
 * Reactive repository interface for Assembly aggregate.
 */
interface AssemblyRepository : ReactiveCrudRepository<Assembly, AssemblyId> {
    /**
     * Find assembly by order ID.
     */
    fun findByOrderId(orderId: OrderId): Mono<Assembly>
    
    /**
     * Find assembly by ID.
     */
    override fun findById(id: AssemblyId): Mono<Assembly>
    
    /**
     * Save an assembly.
     */
    fun save(entity: Assembly): Mono<Assembly>
    
    /**
     * Delete an assembly.
     */
    override fun delete(entity: Assembly): Mono<Void>
}
