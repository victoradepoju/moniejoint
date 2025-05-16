package com.victor.saving_group_service.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupSummaryResponse {
    private String groupId;
    private BigDecimal totalContributions;
    private Integer totalMembers;
    private LocalDateTime nextContributionDate;
}
