package com.wcapp.backend.panini.controller

import com.wcapp.backend.panini.dto.PaniniUserResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * Controlador mock que simula la API pública de Panini.
 * Solo activo cuando app.panini.mock-enabled=true (dev/testing).
 *
 * Permite probar el endpoint /external/ sin necesidad de
 * tener acceso a la API real de Panini.
 *
 * El backend se llama a sí mismo configurando:
 *   app.panini.external.base-url: http://localhost:8080/api/v1/panini/mock
 *   app.panini.external.enabled: true
 */
@RestController
@RequestMapping("/api/v1/panini/mock")
class PaniniMockController(
    @Value("\${app.panini.mock-enabled:false}") private val mockEnabled: Boolean
) {
    private val log = LoggerFactory.getLogger(PaniniMockController::class.java)

    // Datos mock por nickname (simula la base de datos de Panini)
    private val mockDatabase = mapOf(
        "thehackerman777" to MockUserData(
            duplicates = listOf("FWC-001", "FWC-023", "FWC-145", "FWC-345", "FWC-567", "FWC-678", "FWC-789", "FWC-876"),
            missing = listOf("FWC-111", "FWC-222", "FWC-444", "FWC-555", "FWC-777", "FWC-888", "FWC-999"),
            completion = 78
        ),
        "elpepe8659" to MockUserData(
            duplicates = listOf("FWC-012", "FWC-034", "FWC-089", "FWC-156", "FWC-234", "FWC-456"),
            missing = listOf("FWC-001", "FWC-023", "FWC-145", "FWC-345", "FWC-567"),
            completion = 65
        ),
        "messi_fan" to MockUserData(
            duplicates = listOf("FWC-001", "FWC-010", "FWC-050", "FWC-100"),
            missing = listOf("FWC-777", "FWC-888"),
            completion = 92
        )
    )

    /**
     * Simula: GET /api/v1/panini/mock/users/{nickname}/collection
     * Misma ruta que usaría la API real de Panini.
     */
    @GetMapping("/users/{nickname}/collection")
    fun getMockCollection(@PathVariable nickname: String): ResponseEntity<Any> {
        if (!mockEnabled) {
            log.debug("Mock Panini disabled - returning 404")
            return ResponseEntity.notFound().build()
        }

        val data = mockDatabase[nickname.lowercase()]
        if (data == null) {
            log.info("📦 MOCK PANINI: '{}' no encontrado en base mock", nickname)
            return ResponseEntity.status(404).body(mapOf(
                "error" to "UserNotFound",
                "message" to "Nickname '$nickname' not found in Panini database"
            ))
        }

        log.info("📦 MOCK PANINI: '{}' → {} repetidas, {} faltantes",
            nickname, data.duplicates.size, data.missing.size)

        return ResponseEntity.ok(mapOf(
            "duplicates" to data.duplicates,
            "missing" to data.missing,
            "completion" to data.completion
        ))
    }

    /**
     * Health check del mock.
     */
    @GetMapping("/health")
    fun mockHealth(): ResponseEntity<Map<String, Any>> {
        return if (mockEnabled) {
            ResponseEntity.ok(mapOf(
                "status" to "UP",
                "service" to "panini-mock",
                "users" to mockDatabase.size
            ))
        } else {
            ResponseEntity.ok(mapOf(
                "status" to "DISABLED",
                "service" to "panini-mock"
            ))
        }
    }

    private data class MockUserData(
        val duplicates: List<String>,
        val missing: List<String>,
        val completion: Int
    )
}
