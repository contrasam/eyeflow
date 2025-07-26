package com.assignment.eyeflow.domain.model.inventory

import java.time.LocalDateTime
import java.util.UUID

/**
 * Inventory aggregate root representing the inventory management for eyeglass components.
 */
class Inventory private constructor(
    val id: InventoryId,
    val itemType: ItemType,
    val itemCode: String,
    val description: String,
    var quantity: Int,
    val minimumStockLevel: Int,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
) {
    companion object {
        fun create(
            itemType: ItemType,
            itemCode: String,
            description: String,
            initialQuantity: Int,
            minimumStockLevel: Int
        ): Inventory {
            require(initialQuantity >= 0) { "Initial quantity cannot be negative" }
            require(minimumStockLevel >= 0) { "Minimum stock level cannot be negative" }
            
            val now = LocalDateTime.now()
            return Inventory(
                id = InventoryId(UUID.randomUUID()),
                itemType = itemType,
                itemCode = itemCode,
                description = description,
                quantity = initialQuantity,
                minimumStockLevel = minimumStockLevel,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    fun checkAvailability(requiredQuantity: Int): Boolean {
        return quantity >= requiredQuantity
    }

    fun acquire(quantityToAcquire: Int): Boolean {
        require(quantityToAcquire > 0) { "Quantity to acquire must be positive" }
        
        if (quantity < quantityToAcquire) {
            return false
        }
        
        quantity -= quantityToAcquire
        updatedAt = LocalDateTime.now()
        
        return true
    }

    fun restock(quantityToAdd: Int) {
        require(quantityToAdd > 0) { "Quantity to add must be positive" }
        
        quantity += quantityToAdd
        updatedAt = LocalDateTime.now()
    }

    fun isLowOnStock(): Boolean {
        return quantity <= minimumStockLevel
    }
}

data class InventoryId(val value: UUID) {
    override fun toString(): String = value.toString()
}

enum class ItemType {
    FRAME,
    LENS
}

data class SupplierOrder(
    val id: UUID,
    val itemType: ItemType,
    val itemCode: String,
    val quantity: Int,
    val status: SupplierOrderStatus,
    val orderedAt: LocalDateTime
)

enum class SupplierOrderStatus {
    ORDERED,
    RECEIVED,
    CANCELLED
}
