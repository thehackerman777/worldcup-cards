package com.wcapp.backend.panini.connector

import com.wcapp.backend.panini.dto.PaniniUserResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.Duration
import org.springframework.boot.web.client.RestTemplateBuilder

/**
 * Conector para consultar información directamente desde la API pública de Panini.
 *
 * Configuración:
 *   app.panini.external.enabled=true   → intenta API externa
 *   app.panini.external.enabled=false  → salta directo a fallback local
 *
 * Flujo:
 *   1. Si API externa configurada y enabled → intenta conectar
 *   2. Si la API responde → retorna datos frescos
 *   3. Si la API no responde → lanza excepción (el controller decide el fallback)
 */
@Component
class PaniniExternalConnector(
    @Value("\${app.panini.external.enabled:false}") val enabled: Boolean,
    @Value("\${app.panini.external.base-url:}") val baseUrl: String,
    @Value("\${app.panini.external.timeout-ms:10000}") val timeoutMs: Long
) {
    private val log = LoggerFactory.getLogger(PaniniExternalConnector::class.java)
    private val restTemplate = RestTemplateBuilder()
        .setConnectTimeout(Duration.ofMillis(timeoutMs))
        .setReadTimeout(Duration.ofMillis(timeoutMs))
        .build()

    /**
     * Intenta buscar un perfil en la API externa de Panini.
     * Retorna null si no se pudo conectar (para que el controller decida el fallback).
     */
    fun tryLookupUser(nickname: String): PaniniUserResponse? {
        if (!enabled) {
            log.warn("Panini external disabled - skipping API call")
            return null
        }

        if (baseUrl.isBlank()) {
            log.warn("Panini external URL not configured - skipping API call")
            return null
        }

        val url = "$baseUrl/users/$nickname/collection"
        log.info("🌐 Consulting Panini API: $url")

        return try {
            val response = restTemplate.getForEntity(url, PaniniExternalResponse::class.java)

            if (response.statusCode.is2xxSuccessful && response.body != null) {
                val body = response.body!!
                log.info("🌐 Panini API OK: '{}' | {} duplicates, {} missing", nickname,
                    body.duplicates?.size ?: 0, body.missing?.size ?: 0)
                PaniniUserResponse(
                    nickname = nickname,
                    duplicates = body.duplicates ?: emptyList(),
                    missing = body.missing ?: emptyList(),
                    completion = body.completion ?: 0,
                    lastSync = java.time.LocalDateTime.now().toString(),
                    profileFound = true,
                    fromCache = false
                )
            } else {
                log.warn("🌐 Panini API returned {}", response.statusCode)
                null
            }
        } catch (e: Exception) {
            log.warn("🌐 Panini API connection failed for '{}': {}", nickname, e.message)
            null // Return null so controller uses fallback
        }
    }
}

data class PaniniExternalResponse(
    val duplicates: List<String>? = null,
    val missing: List<String>? = null,
    val completion: Int? = null
)

class PaniniExternalUnavailableException(message: String) : RuntimeException(message)
