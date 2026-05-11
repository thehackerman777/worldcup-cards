package com.wcapp.backend.panini.connector

import com.wcapp.backend.panini.dto.PaniniUserResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

/**
 * Conector para consultar información directamente desde la API pública de Panini.
 *
 * Configuración en application.yml:
 *   app.panini.external:
 *     enabled: false
 *     base-url: https://api.panini.com/v1
 *     timeout-ms: 10000
 *
 * Cuando enabled=false, el endpoint retorna error indicando que no está configurado.
 * Cuando enabled=true, intenta conectar con la API externa real de Panini.
 */
@Component
class PaniniExternalConnector(
    @Value("\${app.panini.external.enabled:false}") private val enabled: Boolean,
    @Value("\${app.panini.external.base-url:}") private val baseUrl: String,
    @Value("\${app.panini.external.timeout-ms:10000}") private val timeoutMs: Long
) {
    private val log = LoggerFactory.getLogger(PaniniExternalConnector::class.java)
    private val restTemplate = RestTemplate()

    /**
     * Busca un perfil de usuario directamente en la API pública de Panini.
     *
     * @throws PaniniExternalUnavailableException si la conexión falla o no está configurada
     * @throws PaniniUserNotFoundExternalException si la API responde que el usuario no existe
     */
    fun lookupUser(nickname: String): PaniniUserResponse {
        if (!enabled) {
            log.warn("Panini external connector is DISABLED. Set app.panini.external.enabled=true to activate.")
            throw PaniniExternalUnavailableException(
                "Conexión externa con Panini no configurada. " +
                "Activa app.panini.external.enabled=true en application.yml " +
                "o usa GET /api/v1/panini/local/{nickname} para datos sincronizados."
            )
        }

        if (baseUrl.isBlank()) {
            throw PaniniExternalUnavailableException(
                "URL base de Panini no configurada. Define app.panini.external.base-url."
            )
        }

        val url = "$baseUrl/users/$nickname/collection"
        log.info("Consultando API externa de Panini: $url")

        return try {
            val response = restTemplate.getForEntity(url, PaniniExternalResponse::class.java)

            if (response.statusCode.is2xxSuccessful && response.body != null) {
                val body = response.body!!
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
                throw PaniniExternalUnavailableException(
                    "API de Panini respondió con código ${response.statusCode}"
                )
            }
        } catch (e: PaniniExternalUnavailableException) {
            throw e
        } catch (e: Exception) {
            log.error("Error consultando API externa de Panini para '$nickname'", e)
            throw PaniniExternalUnavailableException(
                "No se pudo conectar con la base de datos de Panini. " +
                "Mensaje: ${e.message ?: "Error desconocido"}. " +
                "Verifica que la API externa esté disponible y configurada correctamente."
            )
        }
    }
}

data class PaniniExternalResponse(
    val duplicates: List<String>? = null,
    val missing: List<String>? = null,
    val completion: Int? = null
)

class PaniniExternalUnavailableException(message: String) : RuntimeException(message)
class PaniniUserNotFoundExternalException(message: String) : RuntimeException(message)
