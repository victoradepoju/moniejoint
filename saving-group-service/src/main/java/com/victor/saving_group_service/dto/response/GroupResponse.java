package com.victor.saving_group_service.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {
    private String groupId;
    private String name;
    private String description;
    private String creatorId;
    private String type;
    private BigDecimal minimumContribution;
    private BigDecimal minimumJoinAmount;
    private Integer maxParticipants;
    private BigDecimal contributionAmount;
    private String frequency;
    private String payoutOrderType;
    private String status;
    private LocalDateTime nextContributionDate;
    private Integer currentRound;
    private BigDecimal targetAmount;
    private Integer memberCount;
    private LocalDateTime createdAt;
}
