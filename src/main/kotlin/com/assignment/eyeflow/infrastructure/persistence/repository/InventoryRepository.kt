package com.assignment.eyeflow.infrastructure.persistence.repository

import com.assignment.eyeflow.domain.model.inventory.Inventory
import com.assignment.eyeflow.domain.model.inventory.InventoryId
import com.assignment.eyeflow.domain.model.inventory.ItemType
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Reactive repository interface for Inventory aggregate.
 */
interface InventoryRepository : ReactiveCrudRepository<Inventory, InventoryId> {
    /**
     * Find inventory items by type.
     */
    fun findByItemType(itemType: ItemType): Flux<Inventory>
    
    /**
     * Find inventory item by item code.
     */
    fun findByItemCode(itemCode: String): Mono<Inventory>
    
    /**
     * Find inventory items with quantity less than or equal to minimum stock level.
     */
    @Query("SELECT * FROM inventory WHERE quantity <= minimum_stock_level")
    fun findLowStockItems(): Flux<Inventory>
    
    /**
     * Find inventory by ID.
     */
    override fun findById(id: InventoryId): Mono<Inventory>
    
    /**
     * Save an inventory item.
     */
    fun save(entity: Inventory): Mono<Inventory>
    
    /**
     * Delete an inventory item.
     */
    override fun delete(entity: Inventory): Mono<Void>
}
