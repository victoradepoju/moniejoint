package com.victor.user_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.victor.user_service.model.UserDetails;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private String id;
    private String username;
    private String email;
    private UserDetails userDetails;
}
