package com.assignment.eyeflow.infrastructure.persistence.repository

import com.assignment.eyeflow.domain.model.order.Order
import com.assignment.eyeflow.domain.model.order.OrderId
import com.assignment.eyeflow.domain.model.order.CustomerId
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Reactive repository interface for Order aggregate.
 */
interface OrderRepository : ReactiveCrudRepository<Order, OrderId> {
    /**
     * Find orders by customer ID.
     */
    fun findByCustomerId(customerId: CustomerId): Flux<Order>
    
    /**
     * Find order by ID.
     */
    override fun findById(id: OrderId): Mono<Order>
    
    /**
     * Save an order.
     */
    fun save(entity: Order): Mono<Order>
    
    /**
     * Delete an order.
     */
    override fun delete(entity: Order): Mono<Void>
}
