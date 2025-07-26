package com.assignment.eyeflow.domain.service.shipping

import com.assignment.eyeflow.domain.model.shipping.Shipping
import com.assignment.eyeflow.domain.model.shipping.ShippingId
import com.assignment.eyeflow.domain.model.shipping.ShippingAddress
import com.assignment.eyeflow.domain.model.order.OrderId
import reactor.core.publisher.Mono

/**
 * Domain service interface for shipping-related operations.
 */
interface ShippingService {
    /**
     * Creates a new shipping record for an order.
     */
    fun createShipping(orderId: OrderId, address: ShippingAddress): Mono<Shipping>
    
    /**
     * Ships an order with tracking information.
     */
    fun shipOrder(shippingId: ShippingId, trackingNumber: String, carrier: String): Mono<Shipping>
    
    /**
     * Marks an order as delivered.
     */
    fun deliverOrder(shippingId: ShippingId): Mono<Shipping>
    
    /**
     * Finds a shipping record by its ID.
     */
    fun findById(shippingId: ShippingId): Mono<Shipping>
    
    /**
     * Finds a shipping record by order ID.
     */
    fun findByOrderId(orderId: OrderId): Mono<Shipping>
}
