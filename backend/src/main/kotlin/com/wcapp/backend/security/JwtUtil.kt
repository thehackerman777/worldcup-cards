package com.wcapp.backend.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtUtil(
    @Value("\${app.jwt.secret}") private val jwtSecret: String,
    @Value("\${app.jwt.expiration-ms}") private val jwtExpirationMs: Long
) {
    private val key: Key by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    fun generateToken(userId: String, username: String, role: String): String {
        val now = Date()
        val expiry = Date(now.time + jwtExpirationMs)

        return Jwts.builder()
            .subject(userId)
            .claim("username", username)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun generateRefreshToken(userId: String): String {
        val now = Date()
        val expiry = Date(now.time + jwtExpirationMs * 7) // 7 days

        return Jwts.builder()
            .subject(userId)
            .claim("type", "refresh")
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun getUserIdFromToken(token: String): String {
        return extractClaims(token).subject
    }

    fun getUsernameFromToken(token: String): String {
        return extractClaims(token).get("username", String::class.java)
    }

    fun getRoleFromToken(token: String): String {
        return extractClaims(token).get("role", String::class.java)
    }

    fun validateToken(token: String): Boolean {
        return try {
            extractClaims(token)
            true
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun isRefreshToken(token: String): Boolean {
        return try {
            val claims = extractClaims(token)
            claims.get("type", String::class.java) == "refresh"
        } catch (e: Exception) {
            false
        }
    }

    fun getExpirationMs(): Long = jwtExpirationMs

    private fun extractClaims(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }
}
