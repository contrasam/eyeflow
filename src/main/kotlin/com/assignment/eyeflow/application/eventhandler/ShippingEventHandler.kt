package com.assignment.eyeflow.application.eventhandler

import com.assignment.eyeflow.domain.event.order.OrderDeliveredEvent
import com.assignment.eyeflow.domain.event.order.OrderShippedEvent
import com.assignment.eyeflow.domain.service.order.OrderService
import com.assignment.eyeflow.infrastructure.messaging.EventBus
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.Disposable
import reactor.core.publisher.Mono

/**
 * Event handler for Shipping events.
 * Subscribes to shipping-related events and processes them accordingly.
 */
@Component
class ShippingEventHandler(
    private val eventBus: EventBus,
    private val orderService: OrderService
) {
    private val logger = LoggerFactory.getLogger(ShippingEventHandler::class.java)
    private val subscriptions = mutableListOf<Disposable>()
    
    @PostConstruct
    fun initialize() {
        logger.info("Initializing Shipping event subscriptions")
        
        // Subscribe to OrderShippedEvent
        subscriptions.add(
            eventBus.subscribe(OrderShippedEvent::class.java)
                .flatMap { handleOrderShipped(it) }
                .subscribe(
                    { logger.debug("Successfully processed OrderShippedEvent") },
                    { error -> logger.error("Error processing OrderShippedEvent", error) }
                )
        )
        
        // Subscribe to OrderDeliveredEvent
        subscriptions.add(
            eventBus.subscribe(OrderDeliveredEvent::class.java)
                .flatMap { handleOrderDelivered(it) }
                .subscribe(
                    { logger.debug("Successfully processed OrderDeliveredEvent") },
                    { error -> logger.error("Error processing OrderDeliveredEvent", error) }
                )
        )
        
        logger.info("Shipping event subscriptions initialized with {} subscriptions", subscriptions.size)
    }
    
    fun handleOrderShipped(event: OrderShippedEvent): Mono<Void> {
        logger.info("Handling OrderShippedEvent: Order ID={}, Tracking Number={}, Carrier={}", 
            event.orderId.value, event.trackingNumber, event.carrier)
        
        // Send shipping notification to customer
        return sendShippingNotification(event)
            .then(updateShippingTracking(event))
            .then(updateExternalSystems(event))
            .then()
    }
    
    fun handleOrderDelivered(event: OrderDeliveredEvent): Mono<Void> {
        logger.info("Handling OrderDeliveredEvent: Order ID={}", event.orderId.value)
        
        // Send delivery notification to customer
        return sendDeliveryNotification(event)
            .then(updateOrderStatus(event))
            .then(collectDeliveryFeedback(event))
            .then()
    }
    
    /**
     * Sends a shipping notification to the customer.
     */
    fun sendShippingNotification(event: OrderShippedEvent): Mono<Void> {
        logger.info("Sending shipping notification for order: {}", event.orderId.value)
        
        // In a real implementation, this would integrate with a notification service
        // to send an email, SMS, or push notification to the customer
        
        return Mono.fromRunnable {
            logger.info("Shipping notification sent for order: {} with tracking number: {} via {}", 
                event.orderId.value, event.trackingNumber, event.carrier)
        }
    }
    
    /**
     * Updates shipping tracking information in any external systems.
     */
    fun updateShippingTracking(event: OrderShippedEvent): Mono<Void> {
        logger.info("Updating shipping tracking for order: {}", event.orderId.value)
        
        // In a real implementation, this might update a customer-facing tracking portal
        // or sync with external logistics systems
        
        return Mono.fromRunnable {
            logger.info("Shipping tracking updated for order: {} with carrier: {}", 
                event.orderId.value, event.carrier)
        }
    }
    
    /**
     * Updates any external systems with shipping information.
     */
    fun updateExternalSystems(event: OrderShippedEvent): Mono<Void> {
        logger.info("Updating external systems for shipped order: {}", event.orderId.value)
        
        // In a real implementation, this might update ERP systems, analytics platforms,
        // or other business systems that need to know about shipments
        
        return Mono.fromRunnable {
            logger.info("External systems updated for shipped order: {}", event.orderId.value)
        }
    }
    
    /**
     * Sends a delivery notification to the customer.
     */
    fun sendDeliveryNotification(event: OrderDeliveredEvent): Mono<Void> {
        logger.info("Sending delivery notification for order: {}", event.orderId.value)
        
        // In a real implementation, this would integrate with a notification service
        // to send an email, SMS, or push notification to the customer
        
        return Mono.fromRunnable {
            logger.info("Delivery notification sent for order: {}", event.orderId.value)
        }
    }
    
    /**
     * Updates the order status to reflect delivery.
     */
    fun updateOrderStatus(event: OrderDeliveredEvent): Mono<Void> {
        logger.info("Updating order status for delivered order: {}", event.orderId.value)
        
        // In a real implementation, this might trigger the order completion process
        // after a certain period post-delivery (to allow for returns/complaints)
        
        // After a grace period, we could automatically complete the order
        return Mono.delay(java.time.Duration.ofDays(7))
            .then(orderService.completeOrder(event.orderId))
            .then()
    }
    
    /**
     * Initiates the process to collect delivery feedback from the customer.
     */
    fun collectDeliveryFeedback(event: OrderDeliveredEvent): Mono<Void> {
        logger.info("Initiating feedback collection for delivered order: {}", event.orderId.value)
        
        // In a real implementation, this might schedule a feedback request to be sent
        // to the customer a few days after delivery
        
        return Mono.fromRunnable {
            logger.info("Feedback collection scheduled for order: {}", event.orderId.value)
        }
    }
}
