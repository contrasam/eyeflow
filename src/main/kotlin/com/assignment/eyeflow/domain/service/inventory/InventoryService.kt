package com.assignment.eyeflow.domain.service.inventory

import com.assignment.eyeflow.domain.model.inventory.Inventory
import com.assignment.eyeflow.domain.model.inventory.InventoryId
import com.assignment.eyeflow.domain.model.inventory.ItemType
import com.assignment.eyeflow.domain.model.inventory.SupplierOrder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Domain service interface for inventory-related operations.
 */
interface InventoryService {
    /**
     * Creates a new inventory item.
     */
    fun createInventoryItem(
        itemType: ItemType,
        itemCode: String,
        description: String,
        initialQuantity: Int,
        minimumStockLevel: Int
    ): Mono<Inventory>
    
    /**
     * Checks availability of a frame in inventory.
     */
    fun checkFrameAvailability(frameCode: String, requiredQuantity: Int): Mono<Boolean>
    
    /**
     * Checks availability of a lens in inventory.
     */
    fun checkLensAvailability(lensCode: String, requiredQuantity: Int): Mono<Boolean>
    
    /**
     * Acquires frames from inventory.
     */
    fun acquireFrame(frameCode: String, quantity: Int): Mono<Boolean>
    
    /**
     * Acquires lenses from inventory.
     */
    fun acquireLens(lensCode: String, quantity: Int): Mono<Boolean>
    
    /**
     * Orders frames from supplier when inventory is low.
     */
    fun orderFrameWithSupplier(frameCode: String, quantity: Int, supplierId: String): Mono<SupplierOrder>
    
    /**
     * Orders lenses from supplier when inventory is low.
     */
    fun orderLensWithSupplier(lensCode: String, quantity: Int, supplierId: String): Mono<SupplierOrder>
    
    /**
     * Restocks inventory with received items.
     */
    fun restockInventory(inventoryId: InventoryId, quantity: Int): Mono<Inventory>
    
    /**
     * Finds an inventory item by its ID.
     */
    fun findById(inventoryId: InventoryId): Mono<Inventory>
    
    /**
     * Finds inventory items by type.
     */
    fun findByItemType(itemType: ItemType): Flux<Inventory>
    
    /**
     * Finds an inventory item by its code.
     */
    fun findByItemCode(itemCode: String): Mono<Inventory>
    
    /**
     * Gets all items with low stock levels.
     */
    fun findLowStockItems(): Flux<Inventory>
}
