package com.victor.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateWalletRequest {
    @NotBlank
    private String userId;
    private String currency = "NGN";
}
