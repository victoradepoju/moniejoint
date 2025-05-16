package com.victor.saving_group_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private SavingGroup group;

    @Column(nullable = false)
    private String userId;

    @Column
    private Integer payoutOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column(nullable = false)
    private LocalDateTime joinDate;

    @Column
    private LocalDateTime leaveDate;

    @CreatedDate
    private LocalDateTime createdAt;

    public enum MemberStatus {
        ACTIVE, LEFT, REMOVED, SUSPENDED
    }
}
