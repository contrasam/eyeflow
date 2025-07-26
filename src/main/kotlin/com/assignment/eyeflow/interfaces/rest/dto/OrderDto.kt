package com.assignment.eyeflow.interfaces.rest.dto

import com.assignment.eyeflow.domain.model.order.Order
import com.assignment.eyeflow.domain.model.order.OrderId
import com.assignment.eyeflow.domain.model.order.OrderStatus
import java.time.LocalDateTime
import java.util.UUID

/**
 * Data Transfer Object for creating a new order.
 */
data class CreateOrderRequest(
    val customerId: UUID,
    val items: List<OrderItemRequest>
)

/**
 * Data Transfer Object for order item in a request.
 */
data class OrderItemRequest(
    val productId: String,
    val frameType: String,
    val lensType: String,
    val quantity: Int,
    val price: Double
)

/**
 * Data Transfer Object for order response.
 */
data class OrderResponse(
    val id: UUID,
    val status: String,
    val customerId: UUID,
    val items: List<OrderItemResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun fromDomain(order: Order): OrderResponse {
            return OrderResponse(
                id = order.id.value,
                status = order.status.name,
                customerId = order.customerId.value,
                items = order.items.map { OrderItemResponse.fromDomain(it) },
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
        }
    }
}

/**
 * Data Transfer Object for order item in a response.
 */
data class OrderItemResponse(
    val productId: String,
    val frameType: String,
    val lensType: String,
    val quantity: Int,
    val price: Double
) {
    companion object {
        fun fromDomain(orderItem: com.assignment.eyeflow.domain.model.order.OrderItem): OrderItemResponse {
            return OrderItemResponse(
                productId = orderItem.productId,
                frameType = orderItem.frameType,
                lensType = orderItem.lensType,
                quantity = orderItem.quantity,
                price = orderItem.price
            )
        }
    }
}

/**
 * Data Transfer Object for canceling an order.
 */
data class CancelOrderRequest(
    val reason: String
)
