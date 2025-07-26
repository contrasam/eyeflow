package com.assignment.eyeflow.application.eventhandler

import com.assignment.eyeflow.domain.event.inventory.*
import com.assignment.eyeflow.domain.model.inventory.ItemType
import com.assignment.eyeflow.domain.service.inventory.InventoryService
import com.assignment.eyeflow.infrastructure.messaging.EventBus
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.Disposable
import reactor.core.publisher.Mono

/**
 * Event handler for Inventory events.
 * Subscribes to Inventory events and processes them accordingly.
 */
@Component
class InventoryEventHandler(
    private val eventBus: EventBus,
    private val inventoryService: InventoryService
) {
    private val logger = LoggerFactory.getLogger(InventoryEventHandler::class.java)
    private val subscriptions = mutableListOf<Disposable>()
    
    // Configuration for automatic reordering
    private val autoReorderThreshold = 0.2 // Reorder when stock is below 20% of minimum
    private val defaultSupplierIds = mapOf(
        ItemType.FRAME to "default-frame-supplier",
        ItemType.LENS to "default-lens-supplier"
    )
    private val defaultReorderQuantity = 10 // Default quantity to reorder
    
    @PostConstruct
    fun initialize() {
        logger.info("Initializing Inventory event subscriptions")
        
        // Subscribe to FrameAvailabilityCheckedEvent
        subscriptions.add(
            eventBus.subscribe(FrameAvailabilityCheckedEvent::class.java)
                .flatMap { handleFrameAvailabilityChecked(it) }
                .subscribe(
                    { logger.debug("Successfully processed FrameAvailabilityCheckedEvent") },
                    { error -> logger.error("Error processing FrameAvailabilityCheckedEvent", error) }
                )
        )
        
        // Subscribe to LensAvailabilityCheckedEvent
        subscriptions.add(
            eventBus.subscribe(LensAvailabilityCheckedEvent::class.java)
                .flatMap { handleLensAvailabilityChecked(it) }
                .subscribe(
                    { logger.debug("Successfully processed LensAvailabilityCheckedEvent") },
                    { error -> logger.error("Error processing LensAvailabilityCheckedEvent", error) }
                )
        )
        
        // Subscribe to InventoryLevelLowEvent
        subscriptions.add(
            eventBus.subscribe(InventoryLevelLowEvent::class.java)
                .flatMap { handleInventoryLevelLow(it) }
                .subscribe(
                    { logger.debug("Successfully processed InventoryLevelLowEvent") },
                    { error -> logger.error("Error processing InventoryLevelLowEvent", error) }
                )
        )
        
        // Subscribe to FrameAcquiredEvent
        subscriptions.add(
            eventBus.subscribe(FrameAcquiredEvent::class.java)
                .flatMap { handleFrameAcquired(it) }
                .subscribe(
                    { logger.debug("Successfully processed FrameAcquiredEvent") },
                    { error -> logger.error("Error processing FrameAcquiredEvent", error) }
                )
        )
        
        // Subscribe to LensAcquiredEvent
        subscriptions.add(
            eventBus.subscribe(LensAcquiredEvent::class.java)
                .flatMap { handleLensAcquired(it) }
                .subscribe(
                    { logger.debug("Successfully processed LensAcquiredEvent") },
                    { error -> logger.error("Error processing LensAcquiredEvent", error) }
                )
        )
        
        // Subscribe to FrameOrderedWithSupplierEvent
        subscriptions.add(
            eventBus.subscribe(FrameOrderedWithSupplierEvent::class.java)
                .flatMap { handleFrameOrderedWithSupplier(it) }
                .subscribe(
                    { logger.debug("Successfully processed FrameOrderedWithSupplierEvent") },
                    { error -> logger.error("Error processing FrameOrderedWithSupplierEvent", error) }
                )
        )
        
        // Subscribe to LensOrderedWithSupplierEvent
        subscriptions.add(
            eventBus.subscribe(LensOrderedWithSupplierEvent::class.java)
                .flatMap { handleLensOrderedWithSupplier(it) }
                .subscribe(
                    { logger.debug("Successfully processed LensOrderedWithSupplierEvent") },
                    { error -> logger.error("Error processing LensOrderedWithSupplierEvent", error) }
                )
        )
        
        logger.info("Inventory event subscriptions initialized with {} subscriptions", subscriptions.size)
    }
    
    fun handleFrameAvailabilityChecked(event: FrameAvailabilityCheckedEvent): Mono<Void> {
        logger.info("Handling FrameAvailabilityCheckedEvent: Inventory ID={}, Frame Code={}, Available={}, Requested={}, Available Quantity={}", 
            event.inventoryId.value, event.frameCode, event.isAvailable, event.requestedQuantity, event.availableQuantity)
        
        // If the frame is not available and the available quantity is very low,
        // we might want to trigger an emergency reorder
        if (!event.isAvailable && event.availableQuantity < event.requestedQuantity) {
            return handleLowFrameInventory(event.frameCode, event.requestedQuantity - event.availableQuantity)
        }
        
        return Mono.empty()
    }
    
    private fun handleLowFrameInventory(frameCode: String, shortfall: Int): Mono<Void> {
        logger.info("Handling low frame inventory for code: {}, shortfall: {}", frameCode, shortfall)
        
        // Calculate reorder quantity based on shortfall, with some buffer
        val reorderQuantity = shortfall + defaultReorderQuantity
        
        // Order from supplier
        return inventoryService.orderFrameWithSupplier(
            frameCode = frameCode,
            quantity = reorderQuantity,
            supplierId = defaultSupplierIds[ItemType.FRAME] ?: "default-frame-supplier"
        ).then()
    }
    
    fun handleLensAvailabilityChecked(event: LensAvailabilityCheckedEvent): Mono<Void> {
        logger.info("Handling LensAvailabilityCheckedEvent: Inventory ID={}, Lens Code={}, Available={}, Requested={}, Available Quantity={}", 
            event.inventoryId.value, event.lensCode, event.isAvailable, event.requestedQuantity, event.availableQuantity)
        
        // If the lens is not available and the available quantity is very low,
        // we might want to trigger an emergency reorder
        if (!event.isAvailable && event.availableQuantity < event.requestedQuantity) {
            return handleLowLensInventory(event.lensCode, event.requestedQuantity - event.availableQuantity)
        }
        
        return Mono.empty()
    }
    
    private fun handleLowLensInventory(lensCode: String, shortfall: Int): Mono<Void> {
        logger.info("Handling low lens inventory for code: {}, shortfall: {}", lensCode, shortfall)
        
        // Calculate reorder quantity based on shortfall, with some buffer
        val reorderQuantity = shortfall + defaultReorderQuantity
        
        // Order from supplier
        return inventoryService.orderLensWithSupplier(
            lensCode = lensCode,
            quantity = reorderQuantity,
            supplierId = defaultSupplierIds[ItemType.LENS] ?: "default-lens-supplier"
        ).then()
    }
    
    fun handleInventoryLevelLow(event: InventoryLevelLowEvent): Mono<Void> {
        logger.info("Handling InventoryLevelLowEvent: Inventory ID={}, Item Type={}, Item Code={}, Current Quantity={}, Minimum Stock Level={}", 
            event.inventoryId.value, event.itemType, event.itemCode, event.currentQuantity, event.minimumStockLevel)
        
        // Check if we're below the auto-reorder threshold
        if (event.currentQuantity < (event.minimumStockLevel * autoReorderThreshold)) {
            logger.info("Inventory for {} {} is critically low. Auto-reordering.", event.itemType, event.itemCode)
            
            // Calculate how much to order based on the minimum stock level
            val reorderQuantity = (event.minimumStockLevel * 2) - event.currentQuantity
            
            // Order from supplier based on item type
            return when (event.itemType) {
                ItemType.FRAME -> inventoryService.orderFrameWithSupplier(
                    frameCode = event.itemCode,
                    quantity = reorderQuantity.toInt(),
                    supplierId = defaultSupplierIds[ItemType.FRAME] ?: "default-frame-supplier"
                ).then()
                
                ItemType.LENS -> inventoryService.orderLensWithSupplier(
                    lensCode = event.itemCode,
                    quantity = reorderQuantity.toInt(),
                    supplierId = defaultSupplierIds[ItemType.LENS] ?: "default-lens-supplier"
                ).then()
                
                else -> {
                    logger.warn("Unknown item type: {}. Cannot auto-reorder.", event.itemType)
                    Mono.empty()
                }
            }
        } else {
            logger.info("Inventory for {} {} is low but above auto-reorder threshold.", event.itemType, event.itemCode)
            return Mono.empty()
        }
    }
    
    fun handleFrameAcquired(event: FrameAcquiredEvent): Mono<Void> {
        logger.info("Handling FrameAcquiredEvent: Inventory ID={}, Frame Code={}, Quantity={}, Remaining Quantity={}", 
            event.inventoryId.value, event.frameCode, event.quantity, event.remainingQuantity)
        
        // Update any tracking or reporting systems
        // For example, we might want to update a dashboard or send notifications
        
        logger.info("Frame {} acquired: {} units. Remaining: {} units", 
            event.frameCode, event.quantity, event.remainingQuantity)
        
        return Mono.empty()
    }
    
    fun handleLensAcquired(event: LensAcquiredEvent): Mono<Void> {
        logger.info("Handling LensAcquiredEvent: Inventory ID={}, Lens Code={}, Quantity={}, Remaining Quantity={}", 
            event.inventoryId.value, event.lensCode, event.quantity, event.remainingQuantity)
        
        // Update any tracking or reporting systems
        // For example, we might want to update a dashboard or send notifications
        
        logger.info("Lens {} acquired: {} units. Remaining: {} units", 
            event.lensCode, event.quantity, event.remainingQuantity)
        
        return Mono.empty()
    }
    
    fun handleFrameOrderedWithSupplier(event: FrameOrderedWithSupplierEvent): Mono<Void> {
        logger.info("Handling FrameOrderedWithSupplierEvent: Supplier Order ID={}, Frame Code={}, Quantity={}, Supplier ID={}", 
            event.supplierOrderId, event.frameCode, event.quantity, event.supplierId)
        
        // Update any tracking or reporting systems for supplier orders
        // For example, we might want to update a procurement dashboard or send notifications
        
        logger.info("Frame order placed with supplier: {} units of {} from supplier {}", 
            event.quantity, event.frameCode, event.supplierId)
        
        // In a real system, we might also want to schedule a follow-up check on the order status
        
        return Mono.empty()
    }
    
    fun handleLensOrderedWithSupplier(event: LensOrderedWithSupplierEvent): Mono<Void> {
        logger.info("Handling LensOrderedWithSupplierEvent: Supplier Order ID={}, Lens Code={}, Quantity={}, Supplier ID={}", 
            event.supplierOrderId, event.lensCode, event.quantity, event.supplierId)
        
        // Update any tracking or reporting systems for supplier orders
        // For example, we might want to update a procurement dashboard or send notifications
        
        logger.info("Lens order placed with supplier: {} units of {} from supplier {}", 
            event.quantity, event.lensCode, event.supplierId)
        
        // In a real system, we might also want to schedule a follow-up check on the order status
        
        return Mono.empty()
    }
}
