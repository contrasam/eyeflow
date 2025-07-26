package com.assignment.eyeflow.domain.event.order

import com.assignment.eyeflow.domain.event.BaseDomainEvent
import com.assignment.eyeflow.domain.model.order.OrderId
import com.assignment.eyeflow.domain.model.order.CustomerId
import java.time.LocalDateTime

/**
 * Event emitted when a new order is placed.
 */
class OrderPlacedEvent(
    val orderId: OrderId,
    val customerId: CustomerId,
    val items: List<OrderItemDto>
) : BaseDomainEvent()

/**
 * Event emitted when an order is confirmed.
 */
class OrderConfirmedEvent(
    val orderId: OrderId
) : BaseDomainEvent()

/**
 * Event emitted when an order is canceled.
 */
class OrderCanceledEvent(
    val orderId: OrderId,
    val reason: String
) : BaseDomainEvent()

/**
 * Event emitted when an order is assembled.
 */
class OrderAssembledEvent(
    val orderId: OrderId
) : BaseDomainEvent()

/**
 * Event emitted when an order is shipped.
 */
class OrderShippedEvent(
    val orderId: OrderId,
    val trackingNumber: String,
    val carrier: String
) : BaseDomainEvent()

/**
 * Event emitted when an order is delivered.
 */
class OrderDeliveredEvent(
    val orderId: OrderId
) : BaseDomainEvent()

/**
 * Event emitted when an order is completed.
 */
class OrderCompletedEvent(
    val orderId: OrderId
) : BaseDomainEvent()

/**
 * Data transfer object for order items in events.
 */
data class OrderItemDto(
    val productId: String,
    val frameType: String,
    val lensType: String,
    val quantity: Int,
    val price: Double
)
