package com.avaneesh.yodha.Eventify.controllers;

import com.avaneesh.yodha.Eventify.dto.request.UserLoginRequest;
import com.avaneesh.yodha.Eventify.dto.request.UserRequest;
import com.avaneesh.yodha.Eventify.dto.response.UserResponse;
import com.avaneesh.yodha.Eventify.services.UserServices;
import com.avaneesh.yodha.Eventify.utils.ApiResponse;
import com.avaneesh.yodha.Eventify.utils.JWTUtility;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/users")
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthUsersController {

    private final UserServices userServices;
    private final JWTUtility jwtUtil;

    public AuthUsersController(UserServices userServices, JWTUtility jwtUtil) {
        this.userServices = userServices;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse userResponse = userServices.createUser(userRequest);
        ApiResponse<UserResponse> response = new ApiResponse<>(true, "User created successfully", userResponse);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> loginUser(@Valid @RequestBody UserLoginRequest userLoginRequest, HttpServletResponse response) {
        String token = userServices.loginUser(userLoginRequest.getEmail(), userLoginRequest.getPassword());
        Cookie cookie = new Cookie("JWT-TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtUtil.getExpiration() / 1000));
        response.addCookie(cookie);
        ApiResponse<String> apiResponse = new ApiResponse<>(true, "User login successful", token);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logoutUser(HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT-TOKEN", null);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
        ApiResponse<String> apiResponse = new ApiResponse<>(true, "Logout successful", null);
        return ResponseEntity.ok(apiResponse);
    }
}
