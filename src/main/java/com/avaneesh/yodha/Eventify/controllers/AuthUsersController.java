package com.avaneesh.yodha.Eventify.controllers;

import com.avaneesh.yodha.Eventify.dto.request.UserLoginRequest;
import com.avaneesh.yodha.Eventify.dto.request.UserRequest;
import com.avaneesh.yodha.Eventify.dto.response.UserResponse;
import com.avaneesh.yodha.Eventify.services.UserServices;
import com.avaneesh.yodha.Eventify.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/users")
public class AuthUsersController {

    @Autowired
    private UserServices userServices;


    @PostMapping()
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse userResponse = userServices.CreateUser(userRequest);
        return ResponseEntity.ok(new ApiResponse<>(true,"User created successfully",userResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> loginUser(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        String jwtCookie  = userServices.loginUser(userLoginRequest.getEmail(), userLoginRequest.getPassword());
        return ResponseEntity.ok()
                .header("Set-Cookie", jwtCookie)
                .body(new ApiResponse<>(true,"User login Success",null));
    }

}
