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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service layer for handling user-related operations like registration and login.
 */
@Service
public class UserServices {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserMapper userMapper;
    private final JWTUtility jwtUtility;
    private final AuthenticationManager authManager;

    public UserServices(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, UserMapper userMapper, JWTUtility jwtUtility, AuthenticationManager authManager) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userMapper = userMapper;
        this.jwtUtility = jwtUtility;
        this.authManager = authManager;
    }

    /**
     * Registers a new user.
     *
     * @param userRequest The DTO containing the new user's details.
     * @return A DTO representing the newly created user.
     * @throws ResourceAlreadyExistsException if a user with the same email already exists.
     */
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        userRepository.getUsersByEmail(userRequest.getEmail()).ifPresent(user -> {
            throw new ResourceAlreadyExistsException("User with email " + userRequest.getEmail() + " already exists");
        });

        Users user = userMapper.toUser(userRequest);
        user.setPassword(bCryptPasswordEncoder.encode(userRequest.getPassword()));
        Users savedUser = userRepository.save(user);

        return userMapper.toUserResponse(savedUser);
    }

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param email    The user's email.
     * @param password The user's password.
     * @return A JWT token string.
     * @throws ResourceNotFoundException if the authentication fails.
     */
    public void loginUser(String email, String password, HttpServletResponse response) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            UserDetailImp userDetails = (UserDetailImp) authentication.getPrincipal();


            Cookie jwtCookie = new Cookie("JWT-TOKEN", jwtUtility.generateToken(userDetails.getUsername(), userDetails.getRoles()));

            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(60 * 60 * 24);
            response.addCookie(jwtCookie);

        } catch (AuthenticationException ex) {
            throw new ResourceNotFoundException("Invalid email or password");
        }
    }
}
