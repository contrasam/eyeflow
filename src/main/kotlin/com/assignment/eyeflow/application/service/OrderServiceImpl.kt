package com.assignment.eyeflow.application.service

import com.assignment.eyeflow.domain.event.order.OrderCanceledEvent
import com.assignment.eyeflow.domain.event.order.OrderCompletedEvent
import com.assignment.eyeflow.domain.event.order.OrderConfirmedEvent
import com.assignment.eyeflow.domain.event.order.OrderPlacedEvent
import com.assignment.eyeflow.domain.event.order.OrderItemDto
import com.assignment.eyeflow.domain.model.order.Order
import com.assignment.eyeflow.domain.model.order.OrderId
import com.assignment.eyeflow.domain.model.order.CustomerId
import com.assignment.eyeflow.domain.model.order.OrderItem
import com.assignment.eyeflow.domain.service.order.OrderService
import com.assignment.eyeflow.infrastructure.messaging.EventBus
import com.assignment.eyeflow.infrastructure.persistence.repository.OrderRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.logging.Logger

/**
 * Implementation of the OrderService that coordinates between the domain model and infrastructure.
 */
@Service
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val eventBus: EventBus
) : OrderService {
    private val logger = Logger.getLogger(OrderServiceImpl::class.java.name)

    override fun placeOrder(customerId: CustomerId, items: List<OrderItem>): Mono<Order> {
        logger.info("Placing order for customer: $customerId with ${items.size} items")
        
        val order = Order.place(customerId, items)
        
        return orderRepository.save(order)
            .flatMap { savedOrder ->
                val event = OrderPlacedEvent(
                    orderId = savedOrder.id,
                    customerId = savedOrder.customerId,
                    items = savedOrder.items.map { OrderItemDto(it.productId, it.frameType, it.lensType, it.quantity, it.price) }
                )
                
                eventBus.publish(event)
                    .thenReturn(savedOrder)
            }
    }

    override fun confirmOrder(orderId: OrderId): Mono<Order> {
        logger.info("Confirming order: $orderId")
        
        return orderRepository.findById(orderId)
            .flatMap { order ->
                order.confirm()
                orderRepository.save(order)
            }
            .flatMap { confirmedOrder ->
                val event = OrderConfirmedEvent(confirmedOrder.id)
                eventBus.publish(event)
                    .thenReturn(confirmedOrder)
            }
    }

    override fun cancelOrder(orderId: OrderId, reason: String): Mono<Order> {
        logger.info("Canceling order: $orderId with reason: $reason")
        
        return orderRepository.findById(orderId)
            .flatMap { order ->
                order.cancel(reason)
                orderRepository.save(order)
            }
            .flatMap { canceledOrder ->
                val event = OrderCanceledEvent(canceledOrder.id, reason)
                eventBus.publish(event)
                    .thenReturn(canceledOrder)
            }
    }

    override fun completeOrder(orderId: OrderId): Mono<Order> {
        logger.info("Completing order: $orderId")
        
        return orderRepository.findById(orderId)
            .flatMap { order ->
                order.complete()
                orderRepository.save(order)
            }
            .flatMap { completedOrder ->
                val event = OrderCompletedEvent(completedOrder.id)
                eventBus.publish(event)
                    .thenReturn(completedOrder)
            }
    }

    override fun findById(orderId: OrderId): Mono<Order> {
        return orderRepository.findById(orderId)
    }
}
