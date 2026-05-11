package com.wcapp.backend.config

import com.wcapp.backend.dto.ErrorResponse
import com.wcapp.backend.panini.connector.PaniniExternalUnavailableException
import com.wcapp.backend.panini.service.SyncExpiredException
import com.wcapp.backend.panini.service.UserNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = ex.message ?: "Invalid request",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ErrorResponse(
                status = HttpStatus.CONFLICT.value(),
                error = "Conflict",
                message = ex.message ?: "Operation not allowed",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorResponse(
                status = HttpStatus.UNAUTHORIZED.value(),
                error = "Unauthorized",
                message = "Credenciales inválidas",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.allErrors.joinToString("; ") {
            when (it) {
                is FieldError -> "${it.field}: ${it.defaultMessage}"
                else -> it.defaultMessage ?: "Validation error"
            }
        }
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Validation Error",
                message = errors,
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                error = "Not Found",
                message = ex.message ?: "Perfil no encontrado",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(PaniniExternalUnavailableException::class)
    fun handlePaniniExternalUnavailable(ex: PaniniExternalUnavailableException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            ErrorResponse(
                status = HttpStatus.SERVICE_UNAVAILABLE.value(),
                error = "External Service Unavailable",
                message = ex.message ?: "API externa de Panini no disponible",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(SyncExpiredException::class)
    fun handleSyncExpired(ex: SyncExpiredException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.GONE).body(
            ErrorResponse(
                status = HttpStatus.GONE.value(),
                error = "Cache Expired",
                message = ex.message ?: "Datos expirados",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "Internal Server Error",
                message = "Ocurrió un error inesperado",
                path = request.requestURI
            )
        )
    }
}
