package com.jlaradev.sealtype.controller

import com.jlaradev.sealtype.dto.request.UserRequest
import com.jlaradev.sealtype.dto.response.UserResponse
import com.jlaradev.sealtype.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @PostMapping
    fun createUser(@Valid @RequestBody request: UserRequest): ResponseEntity<UserResponse> {
        val response = userService.createUser(request)
        return ResponseEntity(response, HttpStatus.CREATED)
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        return userService.getUserById(id)?.let {
            ResponseEntity(it, HttpStatus.OK)
        } ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/username/{username}")
    fun findByUsername(@PathVariable username: String): ResponseEntity<UserResponse> {
        return userService.findByUsername(username)?.let {
            ResponseEntity(it, HttpStatus.OK)
        } ?: ResponseEntity.notFound().build()
    }

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UserRequest
    ): ResponseEntity<UserResponse> {
        val response = userService.updateUser(id, request)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @GetMapping("/search")
    fun searchUsers(@RequestParam(name = "term") searchTerm: String): ResponseEntity<List<UserResponse>> {
        val response = userService.searchUsers(searchTerm)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @GetMapping("/{id}/status")
    fun getUserStatus(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        return userService.getUserStatus(id)?.let {
            ResponseEntity(it, HttpStatus.OK)
        } ?: ResponseEntity.notFound().build()
    }
}



