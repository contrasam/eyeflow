package com.assignment.eyeflow.domain.model.shipping

import com.assignment.eyeflow.domain.model.order.OrderId
import java.time.LocalDateTime
import java.util.UUID

/**
 * Shipping aggregate root representing the shipping process of an eyeglass order.
 */
class Shipping private constructor(
    val id: ShippingId,
    val orderId: OrderId,
    private val state: ShippingState,
    val address: ShippingAddress,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
) {
    val status: ShippingStatus get() = state.status
    val trackingNumber: String? get() = when (state) {
        is ShippingState.Shipped -> state.trackingNumber
        is ShippingState.Delivered -> state.trackingNumber
        else -> null
    }
    val carrier: String? get() = when (state) {
        is ShippingState.Shipped -> state.carrier
        is ShippingState.Delivered -> state.carrier
        else -> null
    }
    
    companion object {
        fun create(orderId: OrderId, address: ShippingAddress): Shipping {
            val now = LocalDateTime.now()
            return Shipping(
                id = ShippingId(UUID.randomUUID()),
                orderId = orderId,
                state = ShippingState.Pending,
                address = address,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    fun ship(trackingNumber: String, carrier: String): Shipping {
        require(state is ShippingState.Pending) { "Shipping can only be initiated when in PENDING status" }
        updatedAt = LocalDateTime.now()
        return Shipping(
            id = ShippingId(UUID.randomUUID()),
            orderId = orderId,
            state = ShippingState.Shipped(trackingNumber, carrier),
            address = address,
            createdAt = this.createdAt,
            updatedAt = updatedAt
        )
    }

    fun deliver(): Shipping {
        require(state is ShippingState.Shipped) { "Order can only be delivered when in SHIPPED status" }
        val shippedState = state as ShippingState.Shipped
        updatedAt = LocalDateTime.now()
        return Shipping(
            id = ShippingId(UUID.randomUUID()),
            orderId = orderId,
            state = ShippingState.Delivered(shippedState.trackingNumber, shippedState.carrier),
            address = address,
            createdAt = this.createdAt,
            updatedAt = updatedAt
        )
    }
}

data class ShippingId(val value: UUID) {
    override fun toString(): String = value.toString()
}

data class ShippingAddress(
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val country: String
)

enum class ShippingStatus {
    PENDING,
    SHIPPED,
    DELIVERED
}

/**
 * Sealed class hierarchy representing the different states of a shipping process.
 */
sealed class ShippingState {
    abstract val status: ShippingStatus
    
    /**
     * Pending state - initial state when shipping is created but not yet shipped.
     */
    object Pending : ShippingState() {
        override val status: ShippingStatus = ShippingStatus.PENDING
    }
    
    /**
     * Shipped state - when the order has been shipped with tracking information.
     */
    data class Shipped(
        val trackingNumber: String,
        val carrier: String
    ) : ShippingState() {
        override val status: ShippingStatus = ShippingStatus.SHIPPED
    }
    
    /**
     * Delivered state - when the order has been delivered to the customer.
     */
    data class Delivered(
        val trackingNumber: String,
        val carrier: String
    ) : ShippingState() {
        override val status: ShippingStatus = ShippingStatus.DELIVERED
    }
}
