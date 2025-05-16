package com.victor.saving_group_service.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberResponse {
    private Long memberId;
    private String groupId;
    private String userId;
    private Integer payoutOrder;
    private String status;
    private LocalDateTime joinDate;
    private LocalDateTime createdAt;
}
