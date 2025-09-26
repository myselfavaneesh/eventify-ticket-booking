package com.avaneesh.yodha.Eventify.dto.response;


import com.avaneesh.yodha.Eventify.enums.Gender;
import com.avaneesh.yodha.Eventify.enums.UserTypes;
import lombok.Data;

@Data
public class UserResponse {
    private long id;
    private String name;
    private String email;
    private String phone;
    private UserTypes userType;
    private Gender gender;
}
