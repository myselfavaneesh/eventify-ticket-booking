package com.avaneesh.yodha.Eventify.controllers;

import com.avaneesh.yodha.Eventify.dto.request.UserLoginRequest;
import com.avaneesh.yodha.Eventify.dto.request.UserRequest;
import com.avaneesh.yodha.Eventify.dto.response.UserResponse;
import com.avaneesh.yodha.Eventify.services.UserServices;
import com.avaneesh.yodha.Eventify.utils.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling user authentication and registration.
 */
@RestController
@RequestMapping("/auth/users")
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthUsersController {

    private final UserServices userServices;

    public AuthUsersController(UserServices userServices) {
        this.userServices = userServices;
    }

    /**
     * Registers a new user in the system.
     *
     * @param userRequest The user details for registration.
     * @return A response entity containing the created user's details.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse userResponse = userServices.createUser(userRequest);
        ApiResponse<UserResponse> response = new ApiResponse<>(true, "User created successfully", userResponse);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and returns a JWT token upon successful login.
     *
     * @param userLoginRequest The user's login credentials (email and password).
     * @return A response entity containing the JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> loginUser(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        String token = userServices.loginUser(userLoginRequest.getEmail(), userLoginRequest.getPassword());
        ApiResponse<String> response = new ApiResponse<>(true, "User login successful", token);
        return ResponseEntity.ok(response);
    }
}
