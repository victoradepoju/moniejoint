package com.victor.saving_group_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinGroupRequest {
    @NotBlank
    private String userId;

    @NotNull
    @PositiveOrZero
    private BigDecimal initialAmount;
}
