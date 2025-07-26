package com.assignment.eyeflow.interfaces.rest

import com.assignment.eyeflow.domain.model.assembly.AssemblyId
import com.assignment.eyeflow.domain.model.assembly.AssemblyComponent
import com.assignment.eyeflow.domain.model.assembly.ComponentType
import com.assignment.eyeflow.domain.model.order.OrderId
import com.assignment.eyeflow.domain.service.assembly.AssemblyService
import com.assignment.eyeflow.interfaces.rest.dto.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * REST controller for assembly-related operations.
 */
@RestController
@RequestMapping("/api/assemblies")
class AssemblyController(private val assemblyService: AssemblyService) {

    /**
     * Creates a new assembly for an order.
     */
    @PostMapping
    fun createAssembly(@RequestBody request: CreateAssemblyRequest): Mono<ResponseEntity<AssemblyResponse>> {
        val orderId = OrderId(request.orderId)
        val components = request.components.map { 
            try {
                AssemblyComponent(
                    id = it.id,
                    type = ComponentType.valueOf(it.type),
                    description = it.description,
                    acquired = false
                )
            } catch (e: IllegalArgumentException) {
                return@map null
            }
        }
        
        // Check if any component had an invalid type
        if (components.contains(null)) {
            return Mono.just(ResponseEntity.badRequest().build())
        }
        
        @Suppress("UNCHECKED_CAST")
        return assemblyService.createAssembly(orderId, components as List<AssemblyComponent>)
            .map { assembly -> ResponseEntity.status(HttpStatus.CREATED).body(AssemblyResponse.fromDomain(assembly)) }
    }

    /**
     * Gets an assembly by ID.
     */
    @GetMapping("/{assemblyId}")
    fun getAssembly(@PathVariable assemblyId: UUID): Mono<ResponseEntity<AssemblyResponse>> {
        return assemblyService.findById(AssemblyId(assemblyId))
            .map { assembly -> ResponseEntity.ok(AssemblyResponse.fromDomain(assembly)) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    /**
     * Gets an assembly by order ID.
     */
    @GetMapping("/order/{orderId}")
    fun getAssemblyByOrderId(@PathVariable orderId: UUID): Mono<ResponseEntity<AssemblyResponse>> {
        return assemblyService.findByOrderId(OrderId(orderId))
            .map { assembly -> ResponseEntity.ok(AssemblyResponse.fromDomain(assembly)) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    /**
     * Starts the assembly process.
     */
    @PostMapping("/{assemblyId}/start")
    fun startAssembly(@PathVariable assemblyId: UUID): Mono<ResponseEntity<AssemblyResponse>> {
        return assemblyService.startAssembly(AssemblyId(assemblyId))
            .map { assembly -> ResponseEntity.ok(AssemblyResponse.fromDomain(assembly)) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    /**
     * Completes the assembly process.
     */
    @PostMapping("/{assemblyId}/complete")
    fun completeAssembly(@PathVariable assemblyId: UUID): Mono<ResponseEntity<AssemblyResponse>> {
        return assemblyService.completeAssembly(AssemblyId(assemblyId))
            .map { assembly -> ResponseEntity.ok(AssemblyResponse.fromDomain(assembly)) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    /**
     * Acquires a component for assembly.
     */
    @PostMapping("/{assemblyId}/components/{componentId}/acquire")
    fun acquireComponent(
        @PathVariable assemblyId: UUID,
        @PathVariable componentId: String
    ): Mono<ResponseEntity<AssemblyResponse>> {
        return assemblyService.acquireComponent(AssemblyId(assemblyId), componentId)
            .map { assembly -> ResponseEntity.ok(AssemblyResponse.fromDomain(assembly)) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }
}
