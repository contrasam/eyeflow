package com.assignment.eyeflow.infrastructure.messaging

import com.assignment.eyeflow.domain.event.DomainEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Interface for the event bus that handles publishing and subscribing to domain events.
 */
interface EventBus {
    /**
     * Publishes an event to the event bus.
     *
     * @param event The domain event to publish
     * @return Mono<Void> that completes when the event is published
     */
    fun publish(event: DomainEvent): Mono<Void>
    
    /**
     * Publishes multiple events to the event bus.
     *
     * @param events The domain events to publish
     * @return Mono<Void> that completes when all events are published
     */
    fun publishAll(events: List<DomainEvent>): Mono<Void>
    
    /**
     * Subscribes to events of a specific type.
     *
     * @param eventType The class of the event type to subscribe to
     * @return Flux of events of the specified type
     */
    fun <T : DomainEvent> subscribe(eventType: Class<T>): Flux<T>
}
