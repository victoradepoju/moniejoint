package com.victor.saving_group_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "saving_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class SavingGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String groupId;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private String creatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupType type;

    @Column(nullable = false)
    private BigDecimal minimumContribution;

    @Column(nullable = false)
    private BigDecimal minimumJoinAmount;

    @Column(nullable = false)
    private Integer maxParticipants;

    @Column(nullable = false)
    private BigDecimal contributionAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContributionFrequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutOrderType payoutOrderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupStatus status;

    @Column(nullable = false)
    private LocalDateTime nextContributionDate;

    @Column
    private Integer currentRound; // For rotating savings

    @Column
    private BigDecimal targetAmount; // For target savings groups

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroupMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroupInvite> invites = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroupContribution> contributions = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum GroupType {
        ROTATING, FIXED_DEPOSIT, TARGET_SAVING, INVESTMENT_CLUB
    }

    public enum ContributionFrequency {
        TWO_MINUTES, DAILY, WEEKLY, BI_WEEKLY, MONTHLY
    }

    public enum PayoutOrderType {
        RANDOM, CREATOR_DEFINED, FIXED_ROTATION, BIDDING
    }

    public enum GroupStatus {
        FORMING, ACTIVE, COMPLETED, SUSPENDED
    }

    // Helper methods
    public boolean isFull() {
        return members.stream().filter(m -> m.getStatus() == GroupMember.MemberStatus.ACTIVE).count() >= maxParticipants;
    }

    public boolean canAcceptNewMembers() {
        return status == GroupStatus.FORMING || status == GroupStatus.ACTIVE;
    }

    public void addMember(GroupMember member) {
        members.add(member);
        member.setGroup(this);
    }

    public void addInvite(GroupInvite invite) {
        invites.add(invite);
        invite.setGroup(this);
    }

    public void addContribution(GroupContribution contribution) {
        contributions.add(contribution);
        contribution.setGroup(this);
    }
}
