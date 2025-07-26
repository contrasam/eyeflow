package com.assignment.eyeflow.domain.event

import java.time.LocalDateTime
import java.util.UUID

/**
 * Base class for all domain events in the system.
 */
interface DomainEvent {
    val eventId: UUID
    val occurredOn: LocalDateTime
}

/**
 * Abstract base implementation of DomainEvent.
 */
abstract class BaseDomainEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredOn: LocalDateTime = LocalDateTime.now()
) : DomainEvent
