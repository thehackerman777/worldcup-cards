package com.wcapp.backend.panini.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Simulador de la API pública de Panini para desarrollo.
 *
 * Cuando PANINI_MOCK_ENABLED=true, este endpoint simula
 * la API que usaría la aplicación oficial de Panini.
 *
 * NO contiene datos reales ni hardcodeados.
 * Solo existe como placeholder para pruebas de conectividad.
 *
 * Para datos reales:
 *   - Usuarios sincronizan su colección vía POST /local/sync
 *   - Luego se consultan vía GET /cloud/{nickname}
 *   - El conector externo puede apuntar a una API real configurando
 *     PANINI_EXTERNAL_URL con la URL de la API de Panini
 */
@RestController
@RequestMapping("/api/v1/panini/mock")
class PaniniMockController(
    @Value("\${app.panini.mock-enabled:false}") private val mockEnabled: Boolean
) {
    private val log = LoggerFactory.getLogger(PaniniMockController::class.java)

    @GetMapping("/users/{nickname}/collection")
    fun getMockCollection(@PathVariable nickname: String): ResponseEntity<Map<String, Any>> {
        log.info("📦 MOCK PANINI: consulta para '{}' (mock activo: {})", nickname, mockEnabled)

        return if (mockEnabled) {
            ResponseEntity.ok(mapOf(
                "message" to "Mock Panini activo. No hay datos reales aquí.",
                "nickname" to nickname,
                "note" to "Para obtener datos reales, el usuario debe sincronizar su colección via POST /api/v1/panini/local/sync. Luego consultar GET /api/v1/panini/cloud/{nickname}.",
                "duplicates" to emptyList<String>(),
                "missing" to emptyList<String>(),
                "completion" to 0
            ))
        } else {
            ResponseEntity.status(404).body(mapOf(
                "error" to "MockDisabled",
                "message" to "Mock Panini desactivado. Configura PANINI_MOCK_ENABLED=true"
            ))
        }
    }

    @GetMapping("/health")
    fun mockHealth(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf(
            "status" to if (mockEnabled) "MOCK_ACTIVE" else "DISABLED",
            "service" to "panini-mock-simulator",
            "note" to "Este mock NO contiene datos reales de usuarios. Solo para pruebas de conectividad.",
            "realDataEndpoint" to "GET /api/v1/panini/cloud/{nickname}",
            "syncEndpoint" to "POST /api/v1/panini/local/sync"
        ))
    }
}
