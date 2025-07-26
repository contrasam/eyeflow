package com.assignment.eyeflow.interfaces.rest.dto

import com.assignment.eyeflow.domain.model.shipping.Shipping
import com.assignment.eyeflow.domain.model.shipping.ShippingAddress
import com.assignment.eyeflow.domain.model.shipping.ShippingState
import java.time.LocalDateTime
import java.util.UUID

/**
 * Data Transfer Object for creating a new shipping record.
 */
data class CreateShippingRequest(
    val orderId: UUID,
    val address: ShippingAddressRequest
)

/**
 * Data Transfer Object for shipping address in a request.
 */
data class ShippingAddressRequest(
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val country: String
)

/**
 * Data Transfer Object for shipping response.
 */
data class ShippingResponse(
    val id: UUID,
    val orderId: UUID,
    val status: String,
    val address: ShippingAddressResponse,
    val trackingNumber: String?,
    val carrier: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun fromDomain(shipping: Shipping): ShippingResponse {
            return ShippingResponse(
                id = shipping.id.value,
                orderId = shipping.orderId.value,
                status = shipping.status.name,
                address = ShippingAddressResponse.fromDomain(shipping.address),
                trackingNumber = shipping.trackingNumber,
                carrier = shipping.carrier,
                createdAt = shipping.createdAt,
                updatedAt = shipping.updatedAt
            )
        }
    }
}

/**
 * Data Transfer Object for shipping address in a response.
 */
data class ShippingAddressResponse(
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val country: String
) {
    companion object {
        fun fromDomain(address: ShippingAddress): ShippingAddressResponse {
            return ShippingAddressResponse(
                street = address.street,
                city = address.city,
                state = address.state,
                postalCode = address.postalCode,
                country = address.country
            )
        }
    }
}

/**
 * Data Transfer Object for shipping an order.
 */
data class ShipOrderRequest(
    val trackingNumber: String,
    val carrier: String
)
