package com.assignment.eyeflow.application.service

import com.assignment.eyeflow.domain.event.order.OrderAssembledEvent
import com.assignment.eyeflow.domain.model.assembly.Assembly
import com.assignment.eyeflow.domain.model.assembly.AssemblyId
import com.assignment.eyeflow.domain.model.assembly.AssemblyComponent
import com.assignment.eyeflow.domain.model.order.OrderId
import com.assignment.eyeflow.domain.service.assembly.AssemblyService
import com.assignment.eyeflow.infrastructure.messaging.EventBus
import com.assignment.eyeflow.infrastructure.persistence.repository.AssemblyRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.logging.Logger

/**
 * Implementation of the AssemblyService that coordinates between the domain model and infrastructure.
 */
@Service
class AssemblyServiceImpl(
    private val assemblyRepository: AssemblyRepository,
    private val eventBus: EventBus
) : AssemblyService {
    private val logger = Logger.getLogger(AssemblyServiceImpl::class.java.name)

    override fun createAssembly(orderId: OrderId, components: List<AssemblyComponent>): Mono<Assembly> {
        logger.info("Creating assembly for order: $orderId with ${components.size} components")
        
        val assembly = Assembly.create(orderId, components)
        
        return assemblyRepository.save(assembly)
    }

    override fun startAssembly(assemblyId: AssemblyId): Mono<Assembly> {
        logger.info("Starting assembly: $assemblyId")
        
        return assemblyRepository.findById(assemblyId)
            .flatMap { assembly ->
                assembly.startAssembly()
                assemblyRepository.save(assembly)
            }
    }

    override fun completeAssembly(assemblyId: AssemblyId): Mono<Assembly> {
        logger.info("Completing assembly: $assemblyId")
        
        return assemblyRepository.findById(assemblyId)
            .flatMap { assembly ->
                assembly.completeAssembly()
                assemblyRepository.save(assembly)
            }
            .flatMap { completedAssembly ->
                val event = OrderAssembledEvent(completedAssembly.orderId)
                eventBus.publish(event)
                    .thenReturn(completedAssembly)
            }
    }

    override fun acquireComponent(assemblyId: AssemblyId, componentId: String): Mono<Assembly> {
        logger.info("Acquiring component: $componentId for assembly: $assemblyId")
        
        return assemblyRepository.findById(assemblyId)
            .flatMap { assembly ->
                assembly.acquireComponent(componentId)
                assemblyRepository.save(assembly)
            }
    }

    override fun findById(assemblyId: AssemblyId): Mono<Assembly> {
        return assemblyRepository.findById(assemblyId)
    }

    override fun findByOrderId(orderId: OrderId): Mono<Assembly> {
        return assemblyRepository.findByOrderId(orderId)
    }
}
