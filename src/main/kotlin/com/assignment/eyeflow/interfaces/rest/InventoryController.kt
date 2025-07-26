package com.assignment.eyeflow.interfaces.rest

import com.assignment.eyeflow.domain.model.inventory.InventoryId
import com.assignment.eyeflow.domain.model.inventory.ItemType
import com.assignment.eyeflow.domain.service.inventory.InventoryService
import com.assignment.eyeflow.interfaces.rest.dto.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * REST controller for inventory-related operations.
 */
@RestController
@RequestMapping("/api/inventory")
class InventoryController(private val inventoryService: InventoryService) {

    /**
     * Creates a new inventory item.
     */
    @PostMapping
    fun createInventoryItem(@RequestBody request: CreateInventoryRequest): Mono<ResponseEntity<InventoryResponse>> {
        val itemType = try {
            ItemType.valueOf(request.itemType)
        } catch (e: IllegalArgumentException) {
            return Mono.just(ResponseEntity.badRequest().build())
        }
        
        return inventoryService.createInventoryItem(
            itemType = itemType,
            itemCode = request.itemCode,
            description = request.description,
            initialQuantity = request.initialQuantity,
            minimumStockLevel = request.minimumStockLevel
        ).map { inventory -> ResponseEntity.status(HttpStatus.CREATED).body(InventoryResponse.fromDomain(inventory)) }
    }

    /**
     * Gets an inventory item by ID.
     */
    @GetMapping("/{inventoryId}")
    fun getInventoryItem(@PathVariable inventoryId: UUID): Mono<ResponseEntity<InventoryResponse>> {
        return inventoryService.findById(InventoryId(inventoryId))
            .map { inventory -> ResponseEntity.ok(InventoryResponse.fromDomain(inventory)) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    /**
     * Gets inventory items by type.
     */
    @GetMapping("/type/{itemType}")
    fun getInventoryItemsByType(@PathVariable itemType: String): Flux<InventoryResponse> {
        val type = try {
            ItemType.valueOf(itemType)
        } catch (e: IllegalArgumentException) {
            return Flux.empty()
        }
        
        return inventoryService.findByItemType(type)
            .map { inventory -> InventoryResponse.fromDomain(inventory) }
    }

    /**
     * Gets inventory items with low stock.
     */
    @GetMapping("/low-stock")
    fun getLowStockItems(): Flux<InventoryResponse> {
        return inventoryService.findLowStockItems()
            .map { inventory -> InventoryResponse.fromDomain(inventory) }
    }

    /**
     * Checks frame availability.
     */
    @PostMapping("/frames/check-availability")
    fun checkFrameAvailability(@RequestBody request: CheckAvailabilityRequest): Mono<ResponseEntity<AvailabilityResponse>> {
        return inventoryService.findByItemCode(request.itemCode)
            .flatMap { inventory ->
                val isAvailable = inventory.checkAvailability(request.quantity)
                
                val response = AvailabilityResponse(
                    itemCode = request.itemCode,
                    isAvailable = isAvailable,
                    requestedQuantity = request.quantity,
                    availableQuantity = inventory.quantity
                )
                
                Mono.just(ResponseEntity.ok(response))
            }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    /**
     * Checks lens availability.
     */
    @PostMapping("/lenses/check-availability")
    fun checkLensAvailability(@RequestBody request: CheckAvailabilityRequest): Mono<ResponseEntity<AvailabilityResponse>> {
        return inventoryService.findByItemCode(request.itemCode)
            .flatMap { inventory ->
                val isAvailable = inventory.checkAvailability(request.quantity)
                
                val response = AvailabilityResponse(
                    itemCode = request.itemCode,
                    isAvailable = isAvailable,
                    requestedQuantity = request.quantity,
                    availableQuantity = inventory.quantity
                )
                
                Mono.just(ResponseEntity.ok(response))
            }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    /**
     * Acquires frames from inventory.
     */
    @PostMapping("/frames/acquire")
    fun acquireFrame(@RequestBody request: AcquireItemRequest): Mono<ResponseEntity<Boolean>> {
        return inventoryService.acquireFrame(request.itemCode, request.quantity)
            .map { success -> ResponseEntity.ok(success) }
    }

    /**
     * Acquires lenses from inventory.
     */
    @PostMapping("/lenses/acquire")
    fun acquireLens(@RequestBody request: AcquireItemRequest): Mono<ResponseEntity<Boolean>> {
        return inventoryService.acquireLens(request.itemCode, request.quantity)
            .map { success -> ResponseEntity.ok(success) }
    }

    /**
     * Restocks inventory.
     */
    @PostMapping("/{inventoryId}/restock")
    fun restockInventory(
        @PathVariable inventoryId: UUID,
        @RequestBody request: RestockInventoryRequest
    ): Mono<ResponseEntity<InventoryResponse>> {
        return inventoryService.restockInventory(InventoryId(inventoryId), request.quantity)
            .map { inventory -> ResponseEntity.ok(InventoryResponse.fromDomain(inventory)) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    /**
     * Orders frames from supplier.
     */
    @PostMapping("/frames/order-from-supplier")
    fun orderFrameFromSupplier(@RequestBody request: OrderFromSupplierRequest): Mono<ResponseEntity<SupplierOrderResponse>> {
        return inventoryService.orderFrameWithSupplier(request.itemCode, request.quantity, request.supplierId)
            .map { supplierOrder ->
                val response = SupplierOrderResponse(
                    id = supplierOrder.id,
                    itemType = supplierOrder.itemType.name,
                    itemCode = supplierOrder.itemCode,
                    quantity = supplierOrder.quantity,
                    status = supplierOrder.status.name,
                    orderedAt = supplierOrder.orderedAt
                )
                ResponseEntity.ok(response)
            }
    }

    /**
     * Orders lenses from supplier.
     */
    @PostMapping("/lenses/order-from-supplier")
    fun orderLensFromSupplier(@RequestBody request: OrderFromSupplierRequest): Mono<ResponseEntity<SupplierOrderResponse>> {
        return inventoryService.orderLensWithSupplier(request.itemCode, request.quantity, request.supplierId)
            .map { supplierOrder ->
                val response = SupplierOrderResponse(
                    id = supplierOrder.id,
                    itemType = supplierOrder.itemType.name,
                    itemCode = supplierOrder.itemCode,
                    quantity = supplierOrder.quantity,
                    status = supplierOrder.status.name,
                    orderedAt = supplierOrder.orderedAt
                )
                ResponseEntity.ok(response)
            }
    }
}
