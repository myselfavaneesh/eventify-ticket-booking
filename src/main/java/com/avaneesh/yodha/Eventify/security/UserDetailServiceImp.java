package com.avaneesh.yodha.Eventify.security;

import com.avaneesh.yodha.Eventify.entities.Users;
import com.avaneesh.yodha.Eventify.exception.ResourceNotFoundException;
import com.avaneesh.yodha.Eventify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailServiceImp implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users users =  userRepository.getUsersByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User not found with email: " + email));
        return new UserDetailImp(users);
    }
}
