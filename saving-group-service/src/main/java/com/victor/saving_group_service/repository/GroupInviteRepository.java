package com.victor.saving_group_service.repository;

import com.victor.saving_group_service.model.GroupInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupInviteRepository extends JpaRepository<GroupInvite, Long> {
    Optional<GroupInvite> findByInviteCode(String inviteCode);

    List<GroupInvite> findByGroupGroupId(String groupId);

    List<GroupInvite> findByInvitedUserId(String userId);

    @Query("SELECT gi FROM GroupInvite gi WHERE gi.invitedUserId = :userId AND gi.status = 'PENDING' AND (gi.expiryDate IS NULL OR gi.expiryDate > CURRENT_TIMESTAMP)")
    List<GroupInvite> findActiveInvitesForUser(String userId);
}
