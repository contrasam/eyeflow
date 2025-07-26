package com.assignment.eyeflow.infrastructure.persistence.repository

import com.assignment.eyeflow.domain.model.shipping.Shipping
import com.assignment.eyeflow.domain.model.shipping.ShippingId
import com.assignment.eyeflow.domain.model.order.OrderId
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

/**
 * Reactive repository interface for Shipping aggregate.
 */
interface ShippingRepository : ReactiveCrudRepository<Shipping, ShippingId> {
    /**
     * Find shipping by order ID.
     */
    fun findByOrderId(orderId: OrderId): Mono<Shipping>
    
    /**
     * Find shipping by ID.
     */
    override fun findById(id: ShippingId): Mono<Shipping>
    
    /**
     * Save a shipping record.
     */
    fun save(entity: Shipping): Mono<Shipping>
    
    /**
     * Delete a shipping record.
     */
    override fun delete(entity: Shipping): Mono<Void>
}
