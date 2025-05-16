package com.victor.saving_group_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContributionDetail {
    private String memberId;
    private BigDecimal amount;
    private boolean success;
    private String errorMessage;
}
