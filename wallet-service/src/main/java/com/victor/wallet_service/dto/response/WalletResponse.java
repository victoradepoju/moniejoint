package com.victor.wallet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private String walletId;
    private String userId;
    private BigDecimal balance;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
}
