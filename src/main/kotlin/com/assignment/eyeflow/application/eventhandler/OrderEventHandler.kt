package com.assignment.eyeflow.application.eventhandler

import com.assignment.eyeflow.domain.event.order.*
import com.assignment.eyeflow.domain.model.order.OrderId
import com.assignment.eyeflow.domain.service.assembly.AssemblyService
import com.assignment.eyeflow.domain.service.inventory.InventoryService
import com.assignment.eyeflow.domain.service.order.OrderService
import com.assignment.eyeflow.infrastructure.messaging.EventBus
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Event handler for Order events.
 * Subscribes to Order events and processes them accordingly.
 */
@Component
class OrderEventHandler(
    private val eventBus: EventBus,
    private val orderService: OrderService,
    private val inventoryService: InventoryService,
    private val assemblyService: AssemblyService
) {
    private val logger = LoggerFactory.getLogger(OrderEventHandler::class.java)
    private val subscriptions = mutableListOf<Disposable>()
    
    @PostConstruct
    fun initialize() {
        logger.info("Initializing Order event subscriptions")
        
        // Subscribe to OrderPlacedEvent
        subscriptions.add(
            eventBus.subscribe(OrderPlacedEvent::class.java)
                .flatMap { handleOrderPlaced(it) }
                .subscribe(
                    { logger.debug("Successfully processed OrderPlacedEvent") },
                    { error -> logger.error("Error processing OrderPlacedEvent", error) }
                )
        )
        
        // Subscribe to OrderConfirmedEvent
        subscriptions.add(
            eventBus.subscribe(OrderConfirmedEvent::class.java)
                .flatMap { handleOrderConfirmed(it) }
                .subscribe(
                    { logger.debug("Successfully processed OrderConfirmedEvent") },
                    { error -> logger.error("Error processing OrderConfirmedEvent", error) }
                )
        )
        
        // Subscribe to OrderCanceledEvent
        subscriptions.add(
            eventBus.subscribe(OrderCanceledEvent::class.java)
                .flatMap { handleOrderCanceled(it) }
                .subscribe(
                    { logger.debug("Successfully processed OrderCanceledEvent") },
                    { error -> logger.error("Error processing OrderCanceledEvent", error) }
                )
        )
        
        // Subscribe to OrderAssembledEvent
        subscriptions.add(
            eventBus.subscribe(OrderAssembledEvent::class.java)
                .flatMap { handleOrderAssembled(it) }
                .subscribe(
                    { logger.debug("Successfully processed OrderAssembledEvent") },
                    { error -> logger.error("Error processing OrderAssembledEvent", error) }
                )
        )
        
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
        
        // Subscribe to OrderCompletedEvent
        subscriptions.add(
            eventBus.subscribe(OrderCompletedEvent::class.java)
                .flatMap { handleOrderCompleted(it) }
                .subscribe(
                    { logger.debug("Successfully processed OrderCompletedEvent") },
                    { error -> logger.error("Error processing OrderCompletedEvent", error) }
                )
        )
        
        logger.info("Order event subscriptions initialized with {} subscriptions", subscriptions.size)
    }
    
    fun handleOrderPlaced(event: OrderPlacedEvent): Mono<Void> {
        logger.info("Handling OrderPlacedEvent: Order ID={}, Customer ID={}, Items={}", 
            event.orderId.value, event.customerId.value, event.items.size)
        
        // Check inventory availability for all items in the order
        val inventoryChecks = event.items.flatMap { item ->
            // For each item, check both frame and lens availability
            listOf(
                inventoryService.checkFrameAvailability(item.frameType, item.quantity),
                inventoryService.checkLensAvailability(item.lensType, item.quantity)
            )
        }
        
        return Flux.concat(inventoryChecks)
            .collectList()
            .flatMap { availabilityResults ->
                // If all inventory checks pass, we can proceed with the order
                if (availabilityResults.all { it }) {
                    logger.info("All items available for order ${event.orderId.value}, proceeding with order")
                    Mono.empty<Void>()
                } else {
                    // Some items are not available, we need to handle this case
                    // This could involve automatically ordering from suppliers or notifying procurement
                    logger.warn("Some items not available for order ${event.orderId.value}, handling unavailability")
                    handleInventoryUnavailability(event)
                }
            }
    }
    
    private fun handleInventoryUnavailability(event: OrderPlacedEvent): Mono<Void> {
        // In a real implementation, this would trigger supplier orders or other procurement processes
        logger.info("Triggering procurement process for unavailable items in order ${event.orderId.value}")
        
        // For each item in the order, check if we need to order from suppliers
        val supplierOrders = event.items.flatMap { item ->
            listOf(
                // Order frames from supplier if needed
                inventoryService.orderFrameWithSupplier(
                    item.frameType, 
                    item.quantity, 
                    "default-frame-supplier"
                ),
                // Order lenses from supplier if needed
                inventoryService.orderLensWithSupplier(
                    item.lensType,
                    item.quantity,
                    "default-lens-supplier"
                )
            )
        }
        
        return Flux.concat(supplierOrders).then()
    }
    
    fun handleOrderConfirmed(event: OrderConfirmedEvent): Mono<Void> {
        logger.info("Handling OrderConfirmedEvent: Order ID={}", event.orderId.value)
        
        // When an order is confirmed, we need to:
        // 1. Acquire inventory items (frames and lenses)
        // 2. Create an assembly for the order
        
        // In a real implementation, we would fetch the order details first
        // For now, we'll just simulate creating an assembly
        return createAssemblyForOrder(event.orderId)
    }
    
    private fun createAssemblyForOrder(orderId: OrderId): Mono<Void> {
        logger.info("Creating assembly for order: {}", orderId.value)
        // In a real implementation, we would:
        // 1. Fetch the order details
        // 2. Acquire the necessary inventory
        // 3. Create assembly components based on order items
        // 4. Create and start the assembly process
        
        // Call the assembly service to create an assembly
        return assemblyService.createAssembly(orderId, emptyList()).then()
    }
    
    fun handleOrderCanceled(event: OrderCanceledEvent): Mono<Void> {
        logger.info("Handling OrderCanceledEvent: Order ID={}, Reason={}", 
            event.orderId.value, event.reason)
        
        // When an order is canceled, we need to:
        // 1. Return any acquired inventory items
        // 2. Cancel any assembly in progress
        // 3. Update any relevant tracking or reporting systems
        
        logger.info("Returning inventory and canceling assembly for order: {}", event.orderId.value)
        return Mono.empty()
    }
    
    fun handleOrderAssembled(event: OrderAssembledEvent): Mono<Void> {
        logger.info("Handling OrderAssembledEvent: Order ID={}", event.orderId.value)
        
        // When an order is assembled, we need to:
        // 1. Update the order status
        // 2. Initiate the shipping process
        
        logger.info("Initiating shipping process for assembled order: {}", event.orderId.value)
        return Mono.empty()
    }
    
    fun handleOrderShipped(event: OrderShippedEvent): Mono<Void> {
        logger.info("Handling OrderShippedEvent: Order ID={}, Tracking Number={}, Carrier={}", 
            event.orderId.value, event.trackingNumber, event.carrier)
        
        // When an order is shipped, we need to:
        // 1. Update the order status
        // 2. Send tracking information to the customer
        // 3. Update any relevant tracking or reporting systems
        
        logger.info("Sending tracking information to customer for order: {}", event.orderId.value)
        return Mono.empty()
    }
    
    fun handleOrderDelivered(event: OrderDeliveredEvent): Mono<Void> {
        logger.info("Handling OrderDeliveredEvent: Order ID={}", event.orderId.value)
        
        // When an order is delivered, we need to:
        // 1. Update the order status
        // 2. Send a delivery confirmation to the customer
        // 3. Initiate any post-delivery processes (e.g., feedback request)
        
        logger.info("Sending delivery confirmation to customer for order: {}", event.orderId.value)
        return Mono.empty()
    }
    
    fun handleOrderCompleted(event: OrderCompletedEvent): Mono<Void> {
        logger.info("Handling OrderCompletedEvent: Order ID={}", event.orderId.value)
        
        // When an order is completed, we need to:
        // 1. Update the order status
        // 2. Archive the order data
        // 3. Update any relevant reporting systems
        
        logger.info("Archiving completed order: {}", event.orderId.value)
        return Mono.empty()
    }
}
