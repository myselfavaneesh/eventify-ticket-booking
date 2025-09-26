package com.avaneesh.yodha.Eventify.mapper;

import com.avaneesh.yodha.Eventify.dto.request.UserRequest;
import com.avaneesh.yodha.Eventify.dto.response.UserResponse;
import com.avaneesh.yodha.Eventify.entities.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
        // Password will be set separately after hashing
    Users toUser(UserRequest dto);

    UserResponse toUserResponse(Users users);
}
