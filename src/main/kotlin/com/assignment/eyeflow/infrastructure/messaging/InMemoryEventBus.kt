package com.assignment.eyeflow.infrastructure.messaging

import com.assignment.eyeflow.domain.event.DomainEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of the EventBus interface.
 * Uses Project Reactor's Sinks to provide reactive event handling.
 */
@Component
class InMemoryEventBus : EventBus {
    private val logger = LoggerFactory.getLogger(InMemoryEventBus::class.java)
    
    // Map to store sinks for each event type
    private val sinks = ConcurrentHashMap<Class<out DomainEvent>, Sinks.Many<DomainEvent>>()
    
    override fun publish(event: DomainEvent): Mono<Void> {
        logger.debug("Publishing event: {}", event)
        return Mono.fromRunnable {
            // Get all event types that are compatible with the event
            val eventTypes = sinks.keys.filter { it.isAssignableFrom(event.javaClass) }
            
            // Publish to all compatible sinks
            eventTypes.forEach { eventType ->
                sinks[eventType]?.emitNext(event) { _, _ -> false }
            }
        }
    }
    
    override fun publishAll(events: List<DomainEvent>): Mono<Void> {
        return Flux.fromIterable(events)
            .flatMap { publish(it) }
            .then()
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : DomainEvent> subscribe(eventType: Class<T>): Flux<T> {
        logger.debug("Subscribing to event type: {}", eventType.simpleName)
        
        // Create a sink for the event type if it doesn't exist
        val sink = sinks.computeIfAbsent(eventType) { 
            Sinks.many().multicast().onBackpressureBuffer<DomainEvent>()
        }
        
        // Return the flux from the sink, casting events to the requested type
        return sink.asFlux().ofType(eventType)
    }
}
