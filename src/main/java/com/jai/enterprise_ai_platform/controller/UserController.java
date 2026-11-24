package com.jai.enterprise_ai_platform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import com.jai.enterprise_ai_platform.entity.User;
import com.jai.enterprise_ai_platform.dto.ApiResponse;
import com.jai.enterprise_ai_platform.dto.UserRequest;
import com.jai.enterprise_ai_platform.dto.UserResponse;
import com.jai.enterprise_ai_platform.exception.UserNotFoundException;
import com.jai.enterprise_ai_platform.service.UserService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@Validated
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

	@GetMapping("/api/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers() {
        List<UserResponse> users = userService.findAll().stream().map(UserController::toResponse).toList();
        return ResponseEntity.ok(success("Users retrieved successfully", users));
	}

    @GetMapping("/api/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        return userService.findById(id)
                .map(UserController::toResponse)
                .map(user -> ResponseEntity.ok(success("User retrieved successfully", user)))
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @PostMapping("/api/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse user = toResponse(userService.save(request));
        return ResponseEntity.status(201).body(success("User created successfully", user));
    }

    @PutMapping("/api/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserRequest request
    ) {
        return userService.update(id, request)
                .map(UserController::toResponse)
                .map(user -> ResponseEntity.ok(success("User updated successfully", user)))
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        if (!userService.deleteById(id)) {
            throw new UserNotFoundException(id);
        }
        return ResponseEntity.noContent().build();
    }

    private static UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(Instant.now(), message, data);
    }
}