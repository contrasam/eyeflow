# Event storming for EyeFlow

## Problem Description
You are tasked with implementing a Kotlin microservice that accepts eyeglass order
requests via a secured REST endpoint and processes them asynchronously by checking lens
and frame inventory, automatically ordering from suppliers when stock is low, and tracking
order status until completion.

## Event Identification

These are all the events identified from the problem description

- OrderPlaced: When customer places the order for the eyeglass
- OrderConfirmed: When availability checks are passed and order is confirmed
- OrderAssembled: When the components for the order are assembled
- OrderCanceled: When there was something unavailable in the order so it cannot be fulfilled
- OrderShipped: When the order is shipped to the customer
- OrderDelivered: When the order is delivered to the customer
- OrderCompleted: When the order is complete after delivery
- FrameAvailabilityChecked: When eyeglass frame is available in the inventory
- LensAvailabilityChecked: When eyeglass lens is available in the inventory
- InventoryLevelLow: When an inventory item has gone below the minimum inventory level
- FrameAcquired: When eyeglass frame is acquired from inventory
- LensAcquired: When eyeglass lens is acquired from inventory
- FrameOrderedWithSupplier: When frames are ordered from supplier
- LensOrderedWithSupplier: When lens are ordered from supplier

## Command identification

From the corresponding events we identify the command

- PlaceOrder -> OrderPlaced
- ConfirmOrder -> OrderConfirmed
- CancelOrder -> OrderCanceled
- AssembleOrder -> OrderAssembled
- ShipOrder -> OrderShipped
- DeliverOrder -> OrderDelivered
- CompleteOrder -> OrderCompleted
- CheckFrameAvailability -> FrameAvailabilityChecked
- CheckLensAvailability -> LensAvailabilityChecked
- AcquireLens -> LensAcquired * InventoryLevelLow
- AcquireFrame -> FrameAcquired * InventoryLevelLow
- OrderFrameWithSupplier -> FrameOrderedWithSupplier
- OrderLensWithSupplier -> LensOrderedWithSupplier 

## Actor Identification

Identifying the actors for each of the commands identified

- (Customer) -> PlaceOrder
- (Order Context) -> ConfirmOrder
- (Order Context, Customer) -> CancelOrder
- (Shipping Context) -> ShipOrder
- (Shipping Context) -> DeliverOrder
- (Order Context) -> CheckFrameAvailability
- (Order Context) -> CheckLensAvailability 
- (Order Context) -> AcquiredLens
- (Order Context) -> AcquireFrame
- (Inventory Context) -> FrameOrderedWithSupplier
- (Inventory Context) -> LensOrderedWithSupplier

## Aggregate Identification

Based on the context of the command deriving the most apt aggregates

- PlaceOrder -> Order -> OrderPlaced
- ConfirmOrder -> Order -> OrderConfirmed
- CancelOrder -> Order -> OrderCanceled
- AssembleOrder -> Assembly -> OrderAssembled
- ShipOrder -> Shipping -> OrderShipped
- DeliverOrder -> Shipping -> OrderDelivered
- CompleteOrder -> Order -> OrderCompleted
- CheckFrameAvailability -> Inventory -> FrameAvailabilityChecked
- CheckLensAvailability -> Inventory -> LensAvailabilityChecked
- AcquireFrame -> Inventory -> FrameAcquired * InventoryLevelLow
- AcquireLens -> Inventory -> LensAcquired * InventoryLevelLow
- OrderFrameWithSupplier -> Inventory -> FrameOrderedWithSupplier
- OrderLensWithSupplier -> Inventory -> LensOrderedWithSupplier

Policy:
- When AcquireLens and AcquireFrame are performed, we check the inventory if the inventory level
  is within the minimum stock level or else we indicate InventoryLevelLow event.
- Order has to be confirmed only after LensAvailabilityChecked & FrameAvailabilityChecked gives a positive 
  inventory value 
- For AssembleOrder command all inventory items have to be acquired (FrameAcquired & LensAcquired) to assembly
- If InventoryLevelLow event is received we have to place order with supplier via OrderFrameWithSupplier or OrderLensWithSupplier 

The aggregates identified from the whole process

- Order
- Assembly
- Shipping
- Inventory

The aggregates identified can be found to form bounded contexts for our system.


