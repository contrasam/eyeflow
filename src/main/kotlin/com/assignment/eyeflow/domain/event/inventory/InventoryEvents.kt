package com.assignment.eyeflow.domain.event.inventory

import com.assignment.eyeflow.domain.event.BaseDomainEvent
import com.assignment.eyeflow.domain.model.inventory.InventoryId
import com.assignment.eyeflow.domain.model.inventory.ItemType

/**
 * Event emitted when frame availability is checked.
 */
class FrameAvailabilityCheckedEvent(
    val inventoryId: InventoryId,
    val frameCode: String,
    val isAvailable: Boolean,
    val requestedQuantity: Int,
    val availableQuantity: Int
) : BaseDomainEvent()

/**
 * Event emitted when lens availability is checked.
 */
class LensAvailabilityCheckedEvent(
    val inventoryId: InventoryId,
    val lensCode: String,
    val isAvailable: Boolean,
    val requestedQuantity: Int,
    val availableQuantity: Int
) : BaseDomainEvent()

/**
 * Event emitted when inventory level is low.
 */
class InventoryLevelLowEvent(
    val inventoryId: InventoryId,
    val itemType: ItemType,
    val itemCode: String,
    val currentQuantity: Int,
    val minimumStockLevel: Int
) : BaseDomainEvent()

/**
 * Event emitted when a frame is acquired from inventory.
 */
class FrameAcquiredEvent(
    val inventoryId: InventoryId,
    val frameCode: String,
    val quantity: Int,
    val remainingQuantity: Int
) : BaseDomainEvent()

/**
 * Event emitted when a lens is acquired from inventory.
 */
class LensAcquiredEvent(
    val inventoryId: InventoryId,
    val lensCode: String,
    val quantity: Int,
    val remainingQuantity: Int
) : BaseDomainEvent()

/**
 * Event emitted when frames are ordered from a supplier.
 */
class FrameOrderedWithSupplierEvent(
    val supplierOrderId: String,
    val frameCode: String,
    val quantity: Int,
    val supplierId: String
) : BaseDomainEvent()

/**
 * Event emitted when lenses are ordered from a supplier.
 */
class LensOrderedWithSupplierEvent(
    val supplierOrderId: String,
    val lensCode: String,
    val quantity: Int,
    val supplierId: String
) : BaseDomainEvent()
