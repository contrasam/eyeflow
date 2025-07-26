package com.assignment.eyeflow.domain.model.order

import java.time.LocalDateTime
import java.util.UUID

/**
 * Order aggregate root representing an eyeglass order in the system.
 */
class Order private constructor(
    val id: OrderId,
    var status: OrderStatus,
    val customerId: CustomerId,
    val items: MutableList<OrderItem>,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
) {
    companion object {
        fun place(customerId: CustomerId, items: List<OrderItem>): Order {
            val now = LocalDateTime.now()
            return Order(
                id = OrderId(UUID.randomUUID()),
                status = OrderStatus.PLACED,
                customerId = customerId,
                items = items.toMutableList(),
                createdAt = now,
                updatedAt = now
            )
        }
    }

    fun confirm() {
        require(status == OrderStatus.PLACED) { "Order can only be confirmed when in PLACED status" }
        status = OrderStatus.CONFIRMED
        updatedAt = LocalDateTime.now()
    }

    fun cancel(reason: String) {
        require(status != OrderStatus.COMPLETED && status != OrderStatus.CANCELLED) {
            "Cannot cancel an order that is already completed or cancelled"
        }
        status = OrderStatus.CANCELLED
        updatedAt = LocalDateTime.now()
    }

    fun complete() {
        require(status == OrderStatus.DELIVERED) { "Order can only be completed when in DELIVERED status" }
        status = OrderStatus.COMPLETED
        updatedAt = LocalDateTime.now()
    }
}

data class OrderId(val value: UUID) {
    override fun toString(): String = value.toString()
}

data class CustomerId(val value: UUID) {
    override fun toString(): String = value.toString()
}

data class OrderItem(
    val productId: String,
    val frameType: String,
    val lensType: String,
    val quantity: Int,
    val price: Double
)

enum class OrderStatus {
    PLACED,
    CONFIRMED,
    CANCELLED,
    ASSEMBLED,
    SHIPPED,
    DELIVERED,
    COMPLETED
}
