package com.wcapp.backend.controller

import com.wcapp.backend.dto.UpdateUserRequest
import com.wcapp.backend.dto.UserResponse
import com.wcapp.backend.security.UserPrincipal
import com.wcapp.backend.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/me")
    fun getMe(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<UserResponse> {
        val response = userService.getUserById(principal.id)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/me")
    fun updateMe(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        val response = userService.updateUser(principal.id, request)
        return ResponseEntity.ok(response)
    }
}
