package com.victor.user_service.request;

import com.victor.user_service.model.UserDetails;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "Id is required")
    private String id;
    private String username;
    private String password;
    private UserDetails userDetails;
}
