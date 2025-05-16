package com.victor.saving_group_service.service;

import com.victor.saving_group_service.dto.request.*;
import com.victor.saving_group_service.dto.response.*;

import java.util.List;

public interface SavingGroupService {
    GroupResponse createGroup(CreateGroupRequest request);
    GroupResponse getGroup(String groupId);
    List<GroupResponse> getUserGroups(String userId);
    // TODO
    GroupResponse updateGroup(String groupId, UpdateGroupRequest request, String requestedBy);
    // TODO
    void deleteGroup(String groupId, String requestedBy);

    GroupMemberResponse joinGroup(String groupId, JoinGroupRequest request);
    GroupMemberResponse joinGroupWithInvite(String inviteCode, JoinGroupRequest request);
    // TODO
    void leaveGroup(String groupId, String userId);
    // TODO
    void removeMember(String groupId, String memberId, String requestedBy);

    GroupInviteResponse createInvite(String groupId, CreateInviteRequest request);
    List<GroupInviteResponse> getGroupInvites(String groupId, String requestedBy);
    List<GroupInviteResponse> getUserInvites(String userId);

    // TODO
    void revokeInvite(String inviteCode, String requestedBy);

    GroupContributionResponse createContribution(String groupId, CreateContributionRequest request);
    List<GroupContributionResponse> getGroupContributions(String groupId);
    List<GroupContributionResponse> getUserContributions(String groupId, String userId);

    void updatePayoutOrder(String groupId, UpdatePayoutOrderRequest request, String requestedBy);
    GroupSummaryResponse getGroupSummary(String groupId);

    void processScheduledContributions();
}
