package com.victor.saving_group_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_invites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class GroupInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private SavingGroup group;

    @Column(nullable = false)
    private String inviteCode;

    @Column(nullable = false)
    private String invitedUserId;

    @Column(nullable = false)
    private String invitedByUserId;

    @Column(nullable = false)
    private BigDecimal minimumAmountRequired;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status;

    @Column
    private LocalDateTime expiryDate;

    @CreatedDate
    private LocalDateTime createdAt;

    public enum InviteStatus {
        PENDING, ACCEPTED, REJECTED, EXPIRED
    }
}
