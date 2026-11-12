package com.annadhata.controller;

import com.annadhata.dto.ApiResponse;
import com.annadhata.dto.AuthRequest;
import com.annadhata.dto.AuthResponse;
import com.annadhata.dto.RegisterRequest;
import com.annadhata.dto.UpdateProfileRequest;
import com.annadhata.dto.UserProfileResponse;
import com.annadhata.model.User;
import com.annadhata.service.AuthService;
import com.annadhata.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication & User Management", description = "Endpoints for user registration, JWT authentication, and profile updates")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user (Donor or Receiver organisation)")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Account registered successfully", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser() {
        User user = SecurityUtils.getCurrentUser();
        UserProfileResponse response = authService.getProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user profile and coordinates")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(@RequestBody UpdateProfileRequest request) {
        User user = SecurityUtils.getCurrentUser();
        UserProfileResponse response = authService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }
}
