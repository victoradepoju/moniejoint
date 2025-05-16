package com.victor.auth_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterDto {
    private String id;
    private String username;
    private String email;
    private UserDetails userDetails;
    private WalletResponse walletInfo;
}
