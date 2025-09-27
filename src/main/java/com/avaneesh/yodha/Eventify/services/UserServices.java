package com.avaneesh.yodha.Eventify.services;

import com.avaneesh.yodha.Eventify.dto.request.UserRequest;
import com.avaneesh.yodha.Eventify.dto.response.UserResponse;
import com.avaneesh.yodha.Eventify.entities.Users;
import com.avaneesh.yodha.Eventify.exception.ResourceAlreadyExistsException;
import com.avaneesh.yodha.Eventify.exception.ResourceNotFoundException;
import com.avaneesh.yodha.Eventify.mapper.UserMapper;
import com.avaneesh.yodha.Eventify.repository.UserRepository;
import com.avaneesh.yodha.Eventify.security.UserDetailImp;
import com.avaneesh.yodha.Eventify.utils.JWTUtility;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class UserServices {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JWTUtility jwtUtility;
    @Autowired
    private AuthenticationManager authManager;

    @Value("${app.jwt.cookie-secure}")
    private boolean cookieSecure;

    @Transactional
    public UserResponse CreateUser(UserRequest userRequest) {
        userRepository.getUsersByEmail(userRequest.getEmail()).ifPresent(user -> {
            System.out.println("User with email " + userRequest.getEmail() + " already exists");
            throw new ResourceAlreadyExistsException("User with email " + userRequest.getEmail() + " already exists");
        });
        Users user = userMapper.toUser(userRequest);
        user.setPassword(bCryptPasswordEncoder.encode(userRequest.getPassword()));
        Users savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    @Transactional
    public String loginUser(String email, String password) {

        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            UserDetailImp userDetails = (UserDetailImp) authentication.getPrincipal();

            String jwtToken = jwtUtility.generateToken(userDetails.getUsername(), userDetails.getRoles());

            ResponseCookie jwtCookie = ResponseCookie.from("JWT-TOKEN", jwtToken)
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .path("/")
                    .maxAge(Duration.ofHours(1))
                    .sameSite("Strict")
                    .build();

            return jwtCookie.toString();

        } catch (AuthenticationException ex) {
            System.out.println("Auth failed: " + ex.getClass().getSimpleName());
            throw new ResourceNotFoundException("Invalid email or password");
        }
    }

}
