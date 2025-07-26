package com.assignment.eyeflow.interfaces.rest

import com.assignment.eyeflow.domain.model.order.CustomerId
import com.assignment.eyeflow.domain.model.order.OrderId
import com.assignment.eyeflow.domain.model.order.OrderItem
import com.assignment.eyeflow.domain.service.order.OrderService
import com.assignment.eyeflow.interfaces.rest.dto.CancelOrderRequest
import com.assignment.eyeflow.interfaces.rest.dto.CreateOrderRequest
import com.assignment.eyeflow.interfaces.rest.dto.OrderResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * REST controller for order-related operations.
 */
@RestController
@RequestMapping("/api/orders")
class OrderController(private val orderService: OrderService) {

    /**
     * Creates a new order.
     */
    @PostMapping
    fun createOrder(@RequestBody request: CreateOrderRequest): Mono<ResponseEntity<OrderResponse>> {
        val customerId = CustomerId(request.customerId)
        val items = request.items.map { 
            OrderItem(
                productId = it.productId,
                frameType = it.frameType,
                lensType = it.lensType,
                quantity = it.quantity,
                price = it.price
            )
        }
        
        return orderService.placeOrder(customerId, items)
            .map { order -> ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.fromDomain(order)) }
    }

    /**
     * Gets an order by ID.
     */
    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: UUID): Mono<ResponseEntity<OrderResponse>> {
        return orderService.findById(OrderId(orderId))
            .map { order -> ResponseEntity.ok(OrderResponse.fromDomain(order)) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    /**
     * Confirms an order.
     */
    @PostMapping("/{orderId}/confirm")
    fun confirmOrder(@PathVariable orderId: UUID): Mono<ResponseEntity<OrderResponse>> {
        return orderService.confirmOrder(OrderId(orderId))
            .map { order -> ResponseEntity.ok(OrderResponse.fromDomain(order)) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    /**
     * Cancels an order.
     */
    @PostMapping("/{orderId}/cancel")
    fun cancelOrder(
        @PathVariable orderId: UUID,
        @RequestBody request: CancelOrderRequest
    ): Mono<ResponseEntity<OrderResponse>> {
        return orderService.cancelOrder(OrderId(orderId), request.reason)
            .map { order -> ResponseEntity.ok(OrderResponse.fromDomain(order)) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    /**
     * Completes an order.
     */
    @PostMapping("/{orderId}/complete")
    fun completeOrder(@PathVariable orderId: UUID): Mono<ResponseEntity<OrderResponse>> {
        return orderService.completeOrder(OrderId(orderId))
            .map { order -> ResponseEntity.ok(OrderResponse.fromDomain(order)) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }
}
