package com.victor.saving_group_service.repository;

import com.victor.saving_group_service.model.GroupMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    @EntityGraph(attributePaths = {"group"})
    List<GroupMember> findByGroupGroupId(String groupId);

    @EntityGraph(attributePaths = {"group"})
    Optional<GroupMember> findByGroupGroupIdAndUserId(String groupId, String userId);

    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.groupId = :groupId AND gm.status = 'ACTIVE'")
    int countActiveMembersByGroupId(String groupId);

//    @EntityGraph(attributePaths = {"group"})
//    List<GroupMember> findByGroupGroupIdAndStatusOrderByPayoutOrderAsc(String groupId, GroupMember.MemberStatus status);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.groupId = :groupId AND gm.status = :status ORDER BY gm.payoutOrder ASC")
    List<GroupMember> findByGroupGroupIdAndStatusOrderByPayoutOrderAsc(
            @Param("groupId") String groupId,
            @Param("status") GroupMember.MemberStatus status);
}
