package com.victor.saving_group_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInviteRequest {
    @NotBlank
    private String invitedUserId;

    @NotBlank
    private String invitedByUserId;

    @NotNull
    @PositiveOrZero
    private BigDecimal minimumAmountRequired;

    private LocalDateTime expiryDate;
}
