package com.victor.saving_group_service.dto.response;

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
public class TransactionResponse {
    private String transactionId;
    private String walletId;
    private BigDecimal amount;
    private String currency;
    private String type;
    private String status;
    private String reference;
    private String description;
    private String initiatedBy;
    private LocalDateTime createdAt;
}
