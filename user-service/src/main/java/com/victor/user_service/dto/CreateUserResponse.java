package com.victor.user_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.victor.user_service.model.UserDetails;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateUserResponse {
    private String id;
    private String username;
    private String email;
    private UserDetails userDetails;
    private WalletResponse walletInfo;
}
