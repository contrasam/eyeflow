package com.assignment.eyeflow.application.service

import com.assignment.eyeflow.domain.event.order.OrderDeliveredEvent
import com.assignment.eyeflow.domain.event.order.OrderShippedEvent
import com.assignment.eyeflow.domain.model.shipping.Shipping
import com.assignment.eyeflow.domain.model.shipping.ShippingId
import com.assignment.eyeflow.domain.model.shipping.ShippingAddress
import com.assignment.eyeflow.domain.model.order.OrderId
import com.assignment.eyeflow.domain.service.shipping.ShippingService
import com.assignment.eyeflow.infrastructure.messaging.EventBus
import com.assignment.eyeflow.infrastructure.persistence.repository.ShippingRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.logging.Logger

/**
 * Implementation of the ShippingService that coordinates between the domain model and infrastructure.
 */
@Service
class ShippingServiceImpl(
    private val shippingRepository: ShippingRepository,
    private val eventBus: EventBus
) : ShippingService {
    private val logger = Logger.getLogger(ShippingServiceImpl::class.java.name)

    override fun createShipping(orderId: OrderId, address: ShippingAddress): Mono<Shipping> {
        logger.info("Creating shipping for order: $orderId")
        
        val shipping = Shipping.create(orderId, address)
        
        return shippingRepository.save(shipping)
    }

    override fun shipOrder(shippingId: ShippingId, trackingNumber: String, carrier: String): Mono<Shipping> {
        logger.info("Shipping order with ID: $shippingId, tracking: $trackingNumber, carrier: $carrier")
        
        return shippingRepository.findById(shippingId)
            .flatMap { shipping ->
                shipping.ship(trackingNumber, carrier)
                shippingRepository.save(shipping)
            }
            .flatMap { shippedOrder ->
                val event = OrderShippedEvent(
                    orderId = shippedOrder.orderId,
                    trackingNumber = trackingNumber,
                    carrier = carrier
                )
                eventBus.publish(event)
                    .thenReturn(shippedOrder)
            }
    }

    override fun deliverOrder(shippingId: ShippingId): Mono<Shipping> {
        logger.info("Marking order as delivered: $shippingId")
        
        return shippingRepository.findById(shippingId)
            .flatMap { shipping ->
                shipping.deliver()
                shippingRepository.save(shipping)
            }
            .flatMap { deliveredOrder ->
                val event = OrderDeliveredEvent(deliveredOrder.orderId)
                eventBus.publish(event)
                    .thenReturn(deliveredOrder)
            }
    }

    override fun findById(shippingId: ShippingId): Mono<Shipping> {
        return shippingRepository.findById(shippingId)
    }

    override fun findByOrderId(orderId: OrderId): Mono<Shipping> {
        return shippingRepository.findByOrderId(orderId)
    }
}
