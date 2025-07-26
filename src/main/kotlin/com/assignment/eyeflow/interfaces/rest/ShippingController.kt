package com.assignment.eyeflow.interfaces.rest

import com.assignment.eyeflow.domain.model.shipping.ShippingId
import com.assignment.eyeflow.domain.model.shipping.ShippingAddress
import com.assignment.eyeflow.domain.model.order.OrderId
import com.assignment.eyeflow.domain.service.shipping.ShippingService
import com.assignment.eyeflow.interfaces.rest.dto.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * REST controller for shipping-related operations.
 */
@RestController
@RequestMapping("/api/shipping")
class ShippingController(private val shippingService: ShippingService) {

    /**
     * Creates a new shipping record for an order.
     */
    @PostMapping
    fun createShipping(@RequestBody request: CreateShippingRequest): Mono<ResponseEntity<ShippingResponse>> {
        val orderId = OrderId(request.orderId)
        val address = ShippingAddress(
            street = request.address.street,
            city = request.address.city,
            state = request.address.state,
            postalCode = request.address.postalCode,
            country = request.address.country
        )
        
        return shippingService.createShipping(orderId, address)
            .map { shipping -> ResponseEntity.status(HttpStatus.CREATED).body(ShippingResponse.fromDomain(shipping)) }
    }

    /**
     * Gets a shipping record by ID.
     */
    @GetMapping("/{shippingId}")
    fun getShipping(@PathVariable shippingId: UUID): Mono<ResponseEntity<ShippingResponse>> {
        return shippingService.findById(ShippingId(shippingId))
            .map { shipping -> ResponseEntity.ok(ShippingResponse.fromDomain(shipping)) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    /**
     * Gets a shipping record by order ID.
     */
    @GetMapping("/order/{orderId}")
    fun getShippingByOrderId(@PathVariable orderId: UUID): Mono<ResponseEntity<ShippingResponse>> {
        return shippingService.findByOrderId(OrderId(orderId))
            .map { shipping -> ResponseEntity.ok(ShippingResponse.fromDomain(shipping)) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    /**
     * Ships an order with tracking information.
     */
    @PostMapping("/{shippingId}/ship")
    fun shipOrder(
        @PathVariable shippingId: UUID,
        @RequestBody request: ShipOrderRequest
    ): Mono<ResponseEntity<ShippingResponse>> {
        return shippingService.shipOrder(ShippingId(shippingId), request.trackingNumber, request.carrier)
            .map { shipping -> ResponseEntity.ok(ShippingResponse.fromDomain(shipping)) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    /**
     * Marks an order as delivered.
     */
    @PostMapping("/{shippingId}/deliver")
    fun deliverOrder(@PathVariable shippingId: UUID): Mono<ResponseEntity<ShippingResponse>> {
        return shippingService.deliverOrder(ShippingId(shippingId))
            .map { shipping -> ResponseEntity.ok(ShippingResponse.fromDomain(shipping)) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }
}
