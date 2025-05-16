package com.victor.saving_group_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateContributionRequest {
    @NotBlank
    private String memberId;

    @NotNull
    @Positive
    private BigDecimal amount;
}
