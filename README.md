# EyeFlow - Eyeglass Order Processing Microservice

## Project Overview
EyeFlow is a Kotlin microservice designed to handle eyeglass order processing asynchronously. The system manages the entire lifecycle of eyeglass orders, from initial placement through inventory checks, assembly, shipping, and delivery.

## Development Approach

### Event Storming
I began the development process with an Event Storming session to identify the key components of the system:

- **Events**: Identified all domain events (OrderPlaced, InventoryLevelLow, OrderShipped, etc.)
- **Commands**: Mapped commands that trigger these events (PlaceOrder, CheckFrameAvailability, etc.)
- **Actors**: Determined which actors initiate commands (Customer, Order Context, Inventory Context, etc.)
- **Aggregates**: Established the main aggregates (Order, Assembly, Shipping, Inventory)

For detailed information about the event storming process and outcomes, see the [Event Storming Document](EVENT_STORMING.md).

### Domain-Driven Design
Based on the event storming results, I structured the project following Domain-Driven Design principles:

- **Bounded Contexts**: Separated the application into distinct bounded contexts (Order, Inventory, Assembly, Shipping)
- **Aggregates**: Implemented aggregate roots for each identified domain
- **Domain Events**: Created event classes for all identified domain events
- **Event Bus**: Implemented an event bus for asynchronous communication between contexts

### Reactive Architecture
The system uses a reactive approach with:

- **Asynchronous Processing**: Non-blocking operations throughout the order lifecycle
- **Event-Driven Communication**: Components communicate via events
- **Reactive Repositories**: Database operations are non-blocking

## Technical Implementation

### Core Components
- **REST API**: Secured endpoints for order placement and management
- **Event Bus**: In-memory event bus for publishing and subscribing to domain events
- **Event Handlers**: Specialized handlers for processing domain events
- **Repositories**: Data access layer for each aggregate
- **Services**: Business logic implementation for each domain

### Key Features
- Asynchronous order processing
- Automatic inventory level monitoring
- Supplier ordering when stock is low
- Order tracking through the entire lifecycle
- Comprehensive event handling

## Project Structure
```
com.assignment.eyeflow/
├── api/                  # REST controllers
├── application/          # Application services and event handlers
├── domain/               # Domain models, events, and interfaces
│   ├── assembly/         # Assembly aggregate
│   ├── inventory/        # Inventory aggregate
│   ├── order/            # Order aggregate
│   └── shipping/         # Shipping aggregate
├── infrastructure/       # Technical implementations
│   ├── config/           # Application configuration
│   ├── event/            # Event bus implementation
│   └── persistence/      # Repository implementations
└── util/                 # Utility classes
```

## Prompts Used for Development

- I have performed an event storming process for the problem description of EyeFlow, you can find the outcome in 
@EVENT_STORMING.md, create a skeleton for the project in kotlin such that the domain is separate from the spring framework, 
it needs to use separate packages for separation, namely domain, infrastructure, application, interfaces etc, follows 
the contexts and aggregated identified in the event storming to create parts of the domain 

The domain should have model, event, service, the model is for the domain models, event for the domain events and 
service for the domain services.

When it  comes to infrastructure, I have two concerns to be fulfilled. I need to set up messaging between the different 
contexts. I am thinking of using an in memory event bus for now and I need reactive persistence to be used for the 
persistence layer to store the aggregates.

When in comes to ports, REST API is the primary form of interfacing with the application, the REST API needs to be 
secured by an authentication scheme, I am thinking of using a Basic Auth for now

- I dont see any part of the application subscribed to the events, so that means the application is not complete right, 
only when other components subscribe to events the flow will work

- Add some seed data to the application during first boot so whoever is testing has some sample data to work with 
persisted in the database



## AI Tooling Used
- **Windsurf**: Used for agentic code assistance
- **Claude 3.7 Sonnet**: Used for code generation and refinement
