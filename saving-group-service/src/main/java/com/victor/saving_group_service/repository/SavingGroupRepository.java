package com.victor.saving_group_service.repository;

import com.victor.saving_group_service.model.SavingGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SavingGroupRepository extends JpaRepository<SavingGroup, Long> {
//    @EntityGraph(attributePaths = {"members", "invites"})
    Optional<SavingGroup> findByGroupId(String groupId);

    Optional<SavingGroup> findByName(String name);

//    @EntityGraph(attributePaths = {"members"})
    List<SavingGroup> findByCreatorId(String creatorId);

//    @EntityGraph(attributePaths = {"members"})
    List<SavingGroup> findByStatus(SavingGroup.GroupStatus status);

//    @EntityGraph(attributePaths = {"members"})
    List<SavingGroup> findByNextContributionDateBeforeAndStatus(LocalDateTime date, SavingGroup.GroupStatus status);

    @Query("SELECT sg FROM SavingGroup sg JOIN sg.members m WHERE m.userId = :userId")
    List<SavingGroup> findGroupsByMemberId(String userId);
}
