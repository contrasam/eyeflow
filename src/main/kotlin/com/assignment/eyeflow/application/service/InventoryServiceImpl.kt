package com.assignment.eyeflow.application.service

import com.assignment.eyeflow.domain.event.inventory.*
import com.assignment.eyeflow.domain.model.inventory.*
import com.assignment.eyeflow.domain.service.inventory.InventoryService
import com.assignment.eyeflow.infrastructure.messaging.EventBus
import com.assignment.eyeflow.infrastructure.persistence.repository.InventoryRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID
import java.util.logging.Logger

/**
 * Implementation of the InventoryService that coordinates between the domain model and infrastructure.
 */
@Service
class InventoryServiceImpl(
    private val inventoryRepository: InventoryRepository,
    private val eventBus: EventBus
) : InventoryService {
    private val logger = Logger.getLogger(InventoryServiceImpl::class.java.name)

    override fun createInventoryItem(
        itemType: ItemType,
        itemCode: String,
        description: String,
        initialQuantity: Int,
        minimumStockLevel: Int
    ): Mono<Inventory> {
        logger.info("Creating inventory item: $itemCode of type: $itemType with initial quantity: $initialQuantity")
        
        val inventory = Inventory.create(
            itemType = itemType,
            itemCode = itemCode,
            description = description,
            initialQuantity = initialQuantity,
            minimumStockLevel = minimumStockLevel
        )
        
        return inventoryRepository.save(inventory)
    }

    override fun checkFrameAvailability(frameCode: String, requiredQuantity: Int): Mono<Boolean> {
        logger.info("Checking frame availability for: $frameCode, quantity: $requiredQuantity")
        
        return inventoryRepository.findByItemCode(frameCode)
            .flatMap { inventory ->
                val isAvailable = inventory.checkAvailability(requiredQuantity)
                
                val event = FrameAvailabilityCheckedEvent(
                    inventoryId = inventory.id,
                    frameCode = frameCode,
                    isAvailable = isAvailable,
                    requestedQuantity = requiredQuantity,
                    availableQuantity = inventory.quantity
                )
                
                eventBus.publish(event)
                    .thenReturn(isAvailable)
            }
            .defaultIfEmpty(false)
    }

    override fun checkLensAvailability(lensCode: String, requiredQuantity: Int): Mono<Boolean> {
        logger.info("Checking lens availability for: $lensCode, quantity: $requiredQuantity")
        
        return inventoryRepository.findByItemCode(lensCode)
            .flatMap { inventory ->
                val isAvailable = inventory.checkAvailability(requiredQuantity)
                
                val event = LensAvailabilityCheckedEvent(
                    inventoryId = inventory.id,
                    lensCode = lensCode,
                    isAvailable = isAvailable,
                    requestedQuantity = requiredQuantity,
                    availableQuantity = inventory.quantity
                )
                
                eventBus.publish(event)
                    .thenReturn(isAvailable)
            }
            .defaultIfEmpty(false)
    }

    override fun acquireFrame(frameCode: String, quantity: Int): Mono<Boolean> {
        logger.info("Acquiring frame: $frameCode, quantity: $quantity")
        
        return inventoryRepository.findByItemCode(frameCode)
            .flatMap { inventory ->
                val acquired = inventory.acquire(quantity)
                
                if (acquired) {
                    inventoryRepository.save(inventory)
                        .flatMap { savedInventory ->
                            val events = mutableListOf<Mono<Void>>()
                            
                            // Emit frame acquired event
                            val acquiredEvent = FrameAcquiredEvent(
                                inventoryId = savedInventory.id,
                                frameCode = frameCode,
                                quantity = quantity,
                                remainingQuantity = savedInventory.quantity
                            )
                            events.add(eventBus.publish(acquiredEvent))
                            
                            // Check if inventory is low and emit event if needed
                            if (savedInventory.isLowOnStock()) {
                                val lowStockEvent = InventoryLevelLowEvent(
                                    inventoryId = savedInventory.id,
                                    itemType = ItemType.FRAME,
                                    itemCode = frameCode,
                                    currentQuantity = savedInventory.quantity,
                                    minimumStockLevel = savedInventory.minimumStockLevel
                                )
                                events.add(eventBus.publish(lowStockEvent))
                            }
                            
                            Flux.concat(events)
                                .then()
                                .thenReturn(true)
                        }
                } else {
                    Mono.just(false)
                }
            }
            .defaultIfEmpty(false)
    }

    override fun acquireLens(lensCode: String, quantity: Int): Mono<Boolean> {
        logger.info("Acquiring lens: $lensCode, quantity: $quantity")
        
        return inventoryRepository.findByItemCode(lensCode)
            .flatMap { inventory ->
                val acquired = inventory.acquire(quantity)
                
                if (acquired) {
                    inventoryRepository.save(inventory)
                        .flatMap { savedInventory ->
                            val events = mutableListOf<Mono<Void>>()
                            
                            // Emit lens acquired event
                            val acquiredEvent = LensAcquiredEvent(
                                inventoryId = savedInventory.id,
                                lensCode = lensCode,
                                quantity = quantity,
                                remainingQuantity = savedInventory.quantity
                            )
                            events.add(eventBus.publish(acquiredEvent))
                            
                            // Check if inventory is low and emit event if needed
                            if (savedInventory.isLowOnStock()) {
                                val lowStockEvent = InventoryLevelLowEvent(
                                    inventoryId = savedInventory.id,
                                    itemType = ItemType.LENS,
                                    itemCode = lensCode,
                                    currentQuantity = savedInventory.quantity,
                                    minimumStockLevel = savedInventory.minimumStockLevel
                                )
                                events.add(eventBus.publish(lowStockEvent))
                            }
                            
                            Flux.concat(events)
                                .then()
                                .thenReturn(true)
                        }
                } else {
                    Mono.just(false)
                }
            }
            .defaultIfEmpty(false)
    }

    override fun orderFrameWithSupplier(frameCode: String, quantity: Int, supplierId: String): Mono<SupplierOrder> {
        logger.info("Ordering frame from supplier: $frameCode, quantity: $quantity, supplier: $supplierId")
        
        val supplierOrder = SupplierOrder(
            id = UUID.randomUUID(),
            itemType = ItemType.FRAME,
            itemCode = frameCode,
            quantity = quantity,
            status = SupplierOrderStatus.ORDERED,
            orderedAt = LocalDateTime.now()
        )
        
        val event = FrameOrderedWithSupplierEvent(
            supplierOrderId = supplierOrder.id.toString(),
            frameCode = frameCode,
            quantity = quantity,
            supplierId = supplierId
        )
        
        return eventBus.publish(event)
            .thenReturn(supplierOrder)
    }

    override fun orderLensWithSupplier(lensCode: String, quantity: Int, supplierId: String): Mono<SupplierOrder> {
        logger.info("Ordering lens from supplier: $lensCode, quantity: $quantity, supplier: $supplierId")
        
        val supplierOrder = SupplierOrder(
            id = UUID.randomUUID(),
            itemType = ItemType.LENS,
            itemCode = lensCode,
            quantity = quantity,
            status = SupplierOrderStatus.ORDERED,
            orderedAt = LocalDateTime.now()
        )
        
        val event = LensOrderedWithSupplierEvent(
            supplierOrderId = supplierOrder.id.toString(),
            lensCode = lensCode,
            quantity = quantity,
            supplierId = supplierId
        )
        
        return eventBus.publish(event)
            .thenReturn(supplierOrder)
    }

    override fun restockInventory(inventoryId: InventoryId, quantity: Int): Mono<Inventory> {
        logger.info("Restocking inventory: $inventoryId with quantity: $quantity")
        
        return inventoryRepository.findById(inventoryId)
            .flatMap { inventory ->
                inventory.restock(quantity)
                inventoryRepository.save(inventory)
            }
    }

    override fun findById(inventoryId: InventoryId): Mono<Inventory> {
        return inventoryRepository.findById(inventoryId)
    }

    override fun findByItemType(itemType: ItemType): Flux<Inventory> {
        return inventoryRepository.findByItemType(itemType)
    }

    override fun findByItemCode(itemCode: String): Mono<Inventory> {
        return inventoryRepository.findByItemCode(itemCode)
    }

    override fun findLowStockItems(): Flux<Inventory> {
        return inventoryRepository.findLowStockItems()
    }
}
