package com.victor.saving_group_service.dto.request;

import com.victor.saving_group_service.model.SavingGroup;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequest {
    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String creatorId;

    @NotNull
    private SavingGroup.GroupType type;

    @NotNull
    @Positive
    private BigDecimal minimumContribution;

    @NotNull
    @PositiveOrZero
    private BigDecimal minimumJoinAmount;

    @NotNull
    @Min(2)
    private Integer maxParticipants;

    @NotNull
    @Positive
    private BigDecimal contributionAmount;

    @NotNull
    private SavingGroup.ContributionFrequency frequency;

    @NotNull
    private SavingGroup.PayoutOrderType payoutOrderType;

    @Positive
    private BigDecimal targetAmount;
}
