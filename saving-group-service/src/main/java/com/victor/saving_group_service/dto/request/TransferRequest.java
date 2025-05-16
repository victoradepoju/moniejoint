package com.victor.saving_group_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    @NotBlank
    private String sourceWalletId;

    @NotBlank
    private String destinationWalletId;

    @NotNull
    @Positive
    private BigDecimal amount;

    private String reference;

    @NotBlank
    private String initiatedBy;
}
