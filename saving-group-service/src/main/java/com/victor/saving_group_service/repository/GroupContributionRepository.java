package com.victor.saving_group_service.repository;

import com.victor.saving_group_service.model.GroupContribution;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupContributionRepository extends JpaRepository<GroupContribution, Long> {
    @EntityGraph(attributePaths = {"group", "member"})
    List<GroupContribution> findByGroupGroupId(String groupId);

    @EntityGraph(attributePaths = {"group", "member"})
    List<GroupContribution> findByGroupGroupIdAndMemberUserId(String groupId, String memberId);

    @EntityGraph(attributePaths = {"group", "member"})
    List<GroupContribution> findByGroupGroupIdAndContributionDateBetween(String groupId, LocalDateTime start, LocalDateTime end);

    Optional<GroupContribution> findByContributionId(String contributionId);
}
