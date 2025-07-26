package com.assignment.eyeflow.domain.service.order

import com.assignment.eyeflow.domain.model.order.Order
import com.assignment.eyeflow.domain.model.order.OrderId
import com.assignment.eyeflow.domain.model.order.CustomerId
import com.assignment.eyeflow.domain.model.order.OrderItem
import reactor.core.publisher.Mono

/**
 * Domain service interface for order-related operations.
 */
interface OrderService {
    /**
     * Places a new order in the system.
     */
    fun placeOrder(customerId: CustomerId, items: List<OrderItem>): Mono<Order>
    
    /**
     * Confirms an order after availability checks.
     */
    fun confirmOrder(orderId: OrderId): Mono<Order>
    
    /**
     * Cancels an order with the specified reason.
     */
    fun cancelOrder(orderId: OrderId, reason: String): Mono<Order>
    
    /**
     * Marks an order as completed after delivery.
     */
    fun completeOrder(orderId: OrderId): Mono<Order>
    
    /**
     * Finds an order by its ID.
     */
    fun findById(orderId: OrderId): Mono<Order>
}
