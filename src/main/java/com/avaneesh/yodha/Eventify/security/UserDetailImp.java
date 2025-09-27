package com.avaneesh.yodha.Eventify.security;

import com.avaneesh.yodha.Eventify.entities.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserDetailImp implements UserDetails {

    private final Users user;

    public UserDetailImp(Users user) {
        this.user = user;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getUserType() == null) {
            return List.of(new SimpleGrantedAuthority("CUSTOMER"));
        }
        return List.of(new SimpleGrantedAuthority(user.getUserType().name()));
    }

    @Override
    public String getPassword() {
       return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    public boolean hasRole(String role) {
        return user.getUserType() != null && user.getUserType().name().equals(role);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public List<String> getRoles() {
        return List.of(user.getUserType().name());
    }


}
