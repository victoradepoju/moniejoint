package com.victor.saving_group_service.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupContributionResponse {
    private String contributionId;
    private String groupId;
    private String memberId;
    private BigDecimal amount;
    private LocalDateTime contributionDate;
    private String status;
    private String transactionReference;
    private LocalDateTime createdAt;
}
