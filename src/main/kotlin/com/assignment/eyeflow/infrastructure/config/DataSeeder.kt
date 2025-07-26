package com.assignment.eyeflow.infrastructure.config

import com.assignment.eyeflow.domain.model.inventory.Inventory
import com.assignment.eyeflow.domain.model.inventory.ItemType
import com.assignment.eyeflow.domain.model.order.CustomerId
import com.assignment.eyeflow.domain.model.order.Order
import com.assignment.eyeflow.domain.model.order.OrderItem
import com.assignment.eyeflow.domain.model.order.OrderStatus
import com.assignment.eyeflow.domain.model.shipping.ShippingAddress
import com.assignment.eyeflow.domain.model.shipping.ShippingStatus
import com.assignment.eyeflow.infrastructure.persistence.repository.AssemblyRepository
import com.assignment.eyeflow.infrastructure.persistence.repository.InventoryRepository
import com.assignment.eyeflow.infrastructure.persistence.repository.OrderRepository
import com.assignment.eyeflow.infrastructure.persistence.repository.ShippingRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

/**
 * Component responsible for seeding initial data into the database during application startup.
 * Only active in non-production environments.
 */
@Component
@Profile("!prod") // Only active in non-production environments
class DataSeeder(
    private val inventoryRepository: InventoryRepository,
    private val orderRepository: OrderRepository,
    private val assemblyRepository: AssemblyRepository,
    private val shippingRepository: ShippingRepository,
    private val databaseClient: DatabaseClient
) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(DataSeeder::class.java)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        logger.info("Starting to seed sample data...")
        
        // Check if data already exists
        inventoryRepository.count()
            .flatMap { count ->
                if (count > 0) {
                    logger.info("Database already contains data. Skipping seed operation.")
                    Mono.empty<Void>()
                } else {
                    logger.info("Seeding sample data into the database...")
                    seedData()
                }
            }
            .onErrorResume { error ->
                logger.error("Error checking database: ${error.message}", error)
                Mono.empty()
            }
            .block() // Block until seeding is complete
            
        logger.info("Sample data seeding process completed.")
    }

    private fun seedData(): Mono<Void> {
        return seedInventory()
            .then(seedOrders())
            .onErrorResume { error ->
                logger.error("Error seeding data: ${error.message}", error)
                Mono.empty()
            }
    }

    private fun seedInventory(): Mono<Void> {
        logger.info("Seeding inventory data...")
        
        val frames = listOf(
            createInventoryItem(ItemType.FRAME, "F001", "Classic Round Frame", 50, 10),
            createInventoryItem(ItemType.FRAME, "F002", "Modern Square Frame", 40, 8),
            createInventoryItem(ItemType.FRAME, "F003", "Vintage Oval Frame", 30, 5),
            createInventoryItem(ItemType.FRAME, "F004", "Sport Wrap Frame", 25, 5),
            createInventoryItem(ItemType.FRAME, "F005", "Luxury Designer Frame", 15, 3)
        )
        
        val lenses = listOf(
            createInventoryItem(ItemType.LENS, "L001", "Standard Single Vision Lens", 100, 20),
            createInventoryItem(ItemType.LENS, "L002", "Progressive Lens", 80, 15),
            createInventoryItem(ItemType.LENS, "L003", "Bifocal Lens", 60, 12),
            createInventoryItem(ItemType.LENS, "L004", "Blue Light Filtering Lens", 70, 15),
            createInventoryItem(ItemType.LENS, "L005", "Photochromic Lens", 50, 10),
            createInventoryItem(ItemType.LENS, "L006", "Polarized Sunglasses Lens", 40, 8)
        )
        
        // Save all inventory items using direct SQL
        val allItems = frames + lenses
        
        return Mono.defer {
            var chain = Mono.empty<Void>()
            
            for (item in allItems) {
                chain = chain.then(saveInventoryItem(item))
            }
            
            chain
        }
    }
    
    private fun saveInventoryItem(item: Inventory): Mono<Void> {
        return databaseClient.sql("""
            INSERT INTO inventory (
                id, item_type, item_code, description, quantity, minimum_stock_level, created_at, updated_at
            ) VALUES (
                :id, :itemType, :itemCode, :description, :quantity, :minimumStockLevel, :createdAt, :updatedAt
            )
        """)
        .bind("id", item.id.value)
        .bind("itemType", item.itemType.name)
        .bind("itemCode", item.itemCode)
        .bind("description", item.description)
        .bind("quantity", item.quantity)
        .bind("minimumStockLevel", item.minimumStockLevel)
        .bind("createdAt", item.createdAt)
        .bind("updatedAt", item.updatedAt)
        .then()
    }

    private fun createInventoryItem(
        itemType: ItemType,
        itemCode: String,
        description: String,
        initialQuantity: Int,
        minimumStockLevel: Int
    ): Inventory {
        return Inventory.create(
            itemType = itemType,
            itemCode = itemCode,
            description = description,
            initialQuantity = initialQuantity,
            minimumStockLevel = minimumStockLevel
        )
    }

    private fun seedOrders(): Mono<Void> {
        logger.info("Seeding order data...")
        
        // Create a few sample customers
        val customer1Id = CustomerId(UUID.randomUUID())
        val customer2Id = CustomerId(UUID.randomUUID())
        val customer3Id = CustomerId(UUID.randomUUID())
        
        // Create order UUIDs
        val order1Id = UUID.randomUUID()
        val order2Id = UUID.randomUUID()
        val order3Id = UUID.randomUUID()
        
        // Define order items
        val order1Items = listOf(
            OrderItem(
                productId = "EYEGLASSES-001",
                frameType = "F001",
                lensType = "L001",
                quantity = 1,
                price = 199.99
            )
        )
        
        val order2Items = listOf(
            OrderItem(
                productId = "EYEGLASSES-002",
                frameType = "F002",
                lensType = "L004",
                quantity = 1,
                price = 249.99
            ),
            OrderItem(
                productId = "EYEGLASSES-003",
                frameType = "F003",
                lensType = "L002",
                quantity = 1,
                price = 299.99
            )
        )
        
        val order3Items = listOf(
            OrderItem(
                productId = "SUNGLASSES-001",
                frameType = "F005",
                lensType = "L006",
                quantity = 1,
                price = 349.99
            )
        )
        
        // Save orders and their items using direct SQL
        return saveOrder(order1Id, customer1Id.value, OrderStatus.PLACED, 199.99)
            .then(saveOrderItems(order1Id, order1Items))
            .then(createShippingForOrder(order1Id))
            .then(saveOrder(order2Id, customer2Id.value, OrderStatus.PLACED, 549.98))
            .then(saveOrderItems(order2Id, order2Items))
            .then(saveOrder(order3Id, customer3Id.value, OrderStatus.PLACED, 349.99))
            .then(saveOrderItems(order3Id, order3Items))
            .then()
    }
    
    private fun saveOrder(orderId: UUID, customerId: UUID, status: OrderStatus, totalPrice: Double): Mono<Void> {
        val now = LocalDateTime.now()
        return databaseClient.sql("""
            INSERT INTO orders (id, status, customer_id, total_price, created_at, updated_at)
            VALUES (:id, :status, :customerId, :totalPrice, :createdAt, :updatedAt)
        """)
        .bind("id", orderId)
        .bind("status", status.name)
        .bind("customerId", customerId)
        .bind("totalPrice", totalPrice)
        .bind("createdAt", now)
        .bind("updatedAt", now)
        .then()
    }
    
    private fun saveOrderItems(orderId: UUID, items: List<OrderItem>): Mono<Void> {
        if (items.isEmpty()) {
            return Mono.empty()
        }
        
        var chain = Mono.empty<Void>()
        
        for (item in items) {
            chain = chain.then(
                databaseClient.sql("""
                    INSERT INTO order_item (id, order_id, product_id, frame_type, lens_type, quantity, price)
                    VALUES (:id, :orderId, :productId, :frameType, :lensType, :quantity, :price)
                """)
                .bind("id", UUID.randomUUID())
                .bind("orderId", orderId)
                .bind("productId", item.productId)
                .bind("frameType", item.frameType)
                .bind("lensType", item.lensType)
                .bind("quantity", item.quantity)
                .bind("price", item.price)
                .then()
            )
        }
        
        return chain
    }
    
    private fun createShippingForOrder(orderId: UUID): Mono<Void> {
        val address = ShippingAddress(
            street = "123 Main St",
            city = "Eyeville",
            state = "CA",
            postalCode = "90210",
            country = "USA"
        )
        
        val shippingId = UUID.randomUUID()
        val now = LocalDateTime.now()
        
        return databaseClient.sql("""
            INSERT INTO shipping (
                id, order_id, status, street, city, state, postal_code, country, created_at, updated_at
            ) VALUES (
                :id, :orderId, :status, :street, :city, :state, :postalCode, :country, :createdAt, :updatedAt
            )
        """)
        .bind("id", shippingId)
        .bind("orderId", orderId)
        .bind("status", ShippingStatus.PENDING.name)
        .bind("street", address.street)
        .bind("city", address.city)
        .bind("state", address.state)
        .bind("postalCode", address.postalCode)
        .bind("country", address.country)
        .bind("createdAt", now)
        .bind("updatedAt", now)
        .then()
    }
}
