package com.assignment.eyeflow.interfaces.rest.dto

import com.assignment.eyeflow.domain.model.inventory.Inventory
import com.assignment.eyeflow.domain.model.inventory.ItemType
import java.time.LocalDateTime
import java.util.UUID

/**
 * Data Transfer Object for creating a new inventory item.
 */
data class CreateInventoryRequest(
    val itemType: String,
    val itemCode: String,
    val description: String,
    val initialQuantity: Int,
    val minimumStockLevel: Int
)

/**
 * Data Transfer Object for inventory response.
 */
data class InventoryResponse(
    val id: UUID,
    val itemType: String,
    val itemCode: String,
    val description: String,
    val quantity: Int,
    val minimumStockLevel: Int,
    val isLowOnStock: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun fromDomain(inventory: Inventory): InventoryResponse {
            return InventoryResponse(
                id = inventory.id.value,
                itemType = inventory.itemType.name,
                itemCode = inventory.itemCode,
                description = inventory.description,
                quantity = inventory.quantity,
                minimumStockLevel = inventory.minimumStockLevel,
                isLowOnStock = inventory.isLowOnStock(),
                createdAt = inventory.createdAt,
                updatedAt = inventory.updatedAt
            )
        }
    }
}

/**
 * Data Transfer Object for checking item availability.
 */
data class CheckAvailabilityRequest(
    val itemCode: String,
    val quantity: Int
)

/**
 * Data Transfer Object for availability response.
 */
data class AvailabilityResponse(
    val itemCode: String,
    val isAvailable: Boolean,
    val requestedQuantity: Int,
    val availableQuantity: Int
)

/**
 * Data Transfer Object for acquiring items from inventory.
 */
data class AcquireItemRequest(
    val itemCode: String,
    val quantity: Int
)

/**
 * Data Transfer Object for restocking inventory.
 */
data class RestockInventoryRequest(
    val quantity: Int
)

/**
 * Data Transfer Object for ordering from supplier.
 */
data class OrderFromSupplierRequest(
    val itemCode: String,
    val quantity: Int,
    val supplierId: String
)

/**
 * Data Transfer Object for supplier order response.
 */
data class SupplierOrderResponse(
    val id: UUID,
    val itemType: String,
    val itemCode: String,
    val quantity: Int,
    val status: String,
    val orderedAt: LocalDateTime
)
