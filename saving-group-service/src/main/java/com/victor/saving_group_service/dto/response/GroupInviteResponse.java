package com.victor.saving_group_service.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupInviteResponse {
    private Long inviteId;
    private String groupId;
    private String inviteCode;
    private String invitedUserId;
    private String invitedByUserId;
    private BigDecimal minimumAmountRequired;
    private String status;
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
}
