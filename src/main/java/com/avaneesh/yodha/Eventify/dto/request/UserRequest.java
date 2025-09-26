package com.avaneesh.yodha.Eventify.dto.request;

import com.avaneesh.yodha.Eventify.enums.Gender;
import com.avaneesh.yodha.Eventify.enums.UserTypes;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    @NotBlank(message = "User name cannot be blank.")
    @Size(min = 3, max = 100, message = "User name must be between 3 and 100 characters.")
    private String name;

    @NotBlank(message = "Email cannot be blank.")
    @Email(message = "Invalid email format.")
    private String email;

    @NotBlank(message = "Password cannot be blank.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character.")
    private String password;

    @NotBlank(message = "Phone number cannot be blank.")
    @Size(min = 10, max = 10, message = "Phone number must be exactly 10 digits.")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number.")
    private String  phone;

    @NotNull(message = "User type cannot be blank.")
    private UserTypes userType; // ADMIN, CUSTOMER

    @NotNull(message = "Gender cannot be blank.")
    private Gender gender;
}
