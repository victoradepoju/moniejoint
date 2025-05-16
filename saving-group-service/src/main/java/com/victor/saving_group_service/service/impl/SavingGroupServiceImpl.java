package com.victor.saving_group_service.service.impl;

import com.victor.saving_group_service.dto.ContributionDetail;
import com.victor.saving_group_service.exception.*;
import com.victor.saving_group_service.model.GroupContribution;
import com.victor.saving_group_service.model.GroupInvite;
import com.victor.saving_group_service.model.GroupMember;
import com.victor.saving_group_service.model.SavingGroup;
import com.victor.saving_group_service.repository.GroupContributionRepository;
import com.victor.saving_group_service.repository.GroupInviteRepository;
import com.victor.saving_group_service.repository.GroupMemberRepository;
import com.victor.saving_group_service.repository.SavingGroupRepository;
import com.victor.saving_group_service.service.SavingGroupService;
import com.victor.saving_group_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.victor.saving_group_service.dto.request.*;
import com.victor.saving_group_service.dto.response.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SavingGroupServiceImpl implements SavingGroupService {
    private final SavingGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final GroupInviteRepository inviteRepository;
    private final GroupContributionRepository contributionRepository;
    private final WalletService walletService;

    @Override
    public GroupResponse createGroup(CreateGroupRequest request) {
        String groupName = request.getName();
        if (groupRepository.findByName(groupName).isPresent()) {
            throw new GroupNameAlreadyExistException("Group with name: " + groupName + " already exists");
        }

        // Validate creator has a wallet with sufficient balance
        WalletResponse userWalletResponse = walletService.getWalletBalanceByUserId(request.getCreatorId());
        BigDecimal creatorBalance = userWalletResponse.getBalance();
        log.info("Creator Balance: {}", creatorBalance);
        if (creatorBalance.compareTo(request.getMinimumJoinAmount()) < 0) {
            throw new InsufficientJoinAmountException("Creator does not have sufficient funds to meet the minimum join amount");
        }

        SavingGroup group = SavingGroup.builder()
                .groupId(UUID.randomUUID().toString())
                .name(request.getName())
                .description(request.getDescription())
                .creatorId(request.getCreatorId())
                .type(request.getType())
                .minimumContribution(request.getMinimumContribution())
                .minimumJoinAmount(request.getMinimumJoinAmount())
                .maxParticipants(request.getMaxParticipants())
                .contributionAmount(request.getContributionAmount())
                .frequency(request.getFrequency())
                .payoutOrderType(request.getPayoutOrderType())
                .status(SavingGroup.GroupStatus.FORMING)
                .nextContributionDate(calculateNextContributionDate(request.getFrequency()))
                .currentRound(1)
                .targetAmount(request.getTargetAmount())
                .build();

        SavingGroup savedGroup = groupRepository.save(group);

        // Creator automatically becomes first member
        joinGroup(savedGroup.getGroupId(), new JoinGroupRequest(
                request.getCreatorId(),
                request.getMinimumJoinAmount()
        ));

        return mapToGroupResponse(savedGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupResponse getGroup(String groupId) {
        SavingGroup group = groupRepository.findByGroupId(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));
        return mapToGroupResponse(group);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getUserGroups(String userId) {
        List<SavingGroup> createdGroups = groupRepository.findByCreatorId(userId);
        List<SavingGroup> memberGroups = groupRepository.findGroupsByMemberId(userId);

        Set<SavingGroup> allGroups = new HashSet<>();
        allGroups.addAll(createdGroups);
        allGroups.addAll(memberGroups);

        return allGroups.stream()
                .map(this::mapToGroupResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GroupResponse updateGroup(String groupId, UpdateGroupRequest request, String requestedBy) {
        return null;
    }

    @Override
    public void deleteGroup(String groupId, String requestedBy) {

    }

    @Override
    public GroupMemberResponse joinGroup(String groupId, JoinGroupRequest request) {
        SavingGroup group = groupRepository.findByGroupId(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));

        validateMemberCanJoin(group, request.getUserId(), group.getMinimumJoinAmount());

        return createGroupMember(group, request.getUserId());
    }

    @Override
    public GroupMemberResponse joinGroupWithInvite(String inviteCode, JoinGroupRequest request) {
        GroupInvite invite = inviteRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new InviteNotFoundException("Invite not found with code: " + inviteCode));

        // Validate the invite
        validateInvite(invite, request.getUserId());

        // Check wallet balance against invite's minimum amount requirement
        WalletResponse userWalletResponse = walletService.getWalletBalanceByUserId(request.getUserId());
        BigDecimal userBalance = userWalletResponse.getBalance();
        if (userBalance.compareTo(invite.getMinimumAmountRequired()) < 0) {
            throw new InsufficientJoinAmountException(
                    String.format("This invite requires at least %s in your wallet to join. Current balance: %s",
                            invite.getMinimumAmountRequired(), userBalance));
        }

        // Mark invite as accepted
        invite.setStatus(GroupInvite.InviteStatus.ACCEPTED);
        inviteRepository.save(invite);

        // Create and return the new group member
        return createGroupMember(invite.getGroup(), request.getUserId());
    }

    private void validateInvite(GroupInvite invite, String userId) {
        // Check if invite is for this user
        if (!invite.getInvitedUserId().equals(userId)) {
            throw new UnauthorizedGroupOperationException("This invite is not for the current user");
        }

        // Check if invite is still pending
        if (invite.getStatus() != GroupInvite.InviteStatus.PENDING) {
            throw new InviteExpiredException("Invite has already been used");
        }

        // Check if invite has expired
        if (invite.getExpiryDate() != null && invite.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InviteExpiredException("Invite has expired");
        }

        // Check if group can still accept members
        SavingGroup group = invite.getGroup();
        if (!group.canAcceptNewMembers()) {
            throw new GroupNotAcceptingMembersException("Group is not currently accepting new members");
        }

        // Check if group is full
        if (group.isFull()) {
            throw new GroupFullException("Group has reached maximum number of participants");
        }

        // Check if user is already a member
        if (memberRepository.findByGroupGroupIdAndUserId(group.getGroupId(), userId).isPresent()) {
            throw new AlreadyGroupMemberException("User is already a member of this group");
        }
    }

    private GroupMemberResponse createGroupMember(SavingGroup group, String userId) {
        GroupMember member = GroupMember.builder()
                .group(group)
                .userId(userId)
                .status(GroupMember.MemberStatus.ACTIVE)
                .joinDate(LocalDateTime.now())
                .build();

        // Set payout order if needed
        if (group.getPayoutOrderType() == SavingGroup.PayoutOrderType.RANDOM) {
            int memberCount = memberRepository.countActiveMembersByGroupId(group.getGroupId());
            member.setPayoutOrder(memberCount + 1);
        }

        GroupMember savedMember = memberRepository.save(member);
        group.addMember(savedMember);

        // If group is now full, activate it
        if (group.isFull() && group.getStatus() == SavingGroup.GroupStatus.FORMING) {
            group.setStatus(SavingGroup.GroupStatus.ACTIVE);
            initializePayoutOrder(group);
        }

        groupRepository.save(group);

        return mapToGroupMemberResponse(savedMember);
    }


    @Override
    public void leaveGroup(String groupId, String userId) {

    }

    @Override
    public void removeMember(String groupId, String memberId, String requestedBy) {

    }

    @Override
    public GroupInviteResponse createInvite(String groupId, CreateInviteRequest request) {
        if (Objects.equals(request.getInvitedByUserId(), request.getInvitedUserId())) {
            throw new SelfInvitationException("Cannot invite yourself to a group");
        }
        SavingGroup group = groupRepository.findByGroupId(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));

        // Verify requester is group creator or admin
        if (!group.getCreatorId().equals(request.getInvitedByUserId())) {
            throw new UnauthorizedGroupOperationException("Only group creator can invite members");
        }

        // TODO: Check user balance

        // Verify group can accept new members
        if (!group.canAcceptNewMembers()) {
            throw new GroupNotAcceptingMembersException("Group is not currently accepting new members");
        }

        GroupInvite invite = GroupInvite.builder()
                .group(group)
                .inviteCode(String.format("%06d", new Random().nextInt(1000000)))
                .invitedUserId(request.getInvitedUserId())
                .invitedByUserId(request.getInvitedByUserId())
                .minimumAmountRequired(request.getMinimumAmountRequired())
                .status(GroupInvite.InviteStatus.PENDING)
                .expiryDate(LocalDateTime.now().plusDays(7)) // Default 7-day expiry
                .build();

        GroupInvite savedInvite = inviteRepository.save(invite);
        group.addInvite(savedInvite);
        groupRepository.save(group);

        return mapToGroupInviteResponse(savedInvite);
    }

    @Override
    public List<GroupInviteResponse> getGroupInvites(String groupId, String requestedBy) {
        return List.of();
    }

    @Override
    public List<GroupInviteResponse> getUserInvites(String userId) {
        return List.of();
    }

    @Override
    public void revokeInvite(String inviteCode, String requestedBy) {

    }

    @Override
    public GroupContributionResponse createContribution(String groupId, CreateContributionRequest request) {
        SavingGroup group = groupRepository.findByGroupId(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));

        GroupMember member = memberRepository.findByGroupGroupIdAndUserId(groupId, request.getMemberId())
                .orElseThrow(() -> new GroupNotFoundException("Member not found in group"));

        if (member.getStatus() != GroupMember.MemberStatus.ACTIVE) {
            throw new InvalidContributionException("Only active members can contribute");
        }

        if (request.getAmount().compareTo(group.getMinimumContribution()) < 0) {
            throw new InvalidContributionException(
                    String.format("Contribution amount must be at least %s", group.getMinimumContribution()));
        }

        // Process payment (in a real implementation, this would use wallet service)
        GroupContribution contribution = GroupContribution.builder()
                .contributionId(UUID.randomUUID().toString())
                .group(group)
                .member(member)
                .amount(request.getAmount())
                .contributionDate(LocalDateTime.now())
                .status(GroupContribution.ContributionStatus.COMPLETED)
                .transactionReference(UUID.randomUUID().toString()) // Mock transaction reference
                .build();

        GroupContribution savedContribution = contributionRepository.save(contribution);
        group.addContribution(savedContribution);
        groupRepository.save(group);

        return mapToGroupContributionResponse(savedContribution);
    }

    @Override
    public List<GroupContributionResponse> getGroupContributions(String groupId) {
        return List.of();
    }

    @Override
    public List<GroupContributionResponse> getUserContributions(String groupId, String userId) {
        return List.of();
    }

    @Override
    public void updatePayoutOrder(String groupId, UpdatePayoutOrderRequest request, String requestedBy) {
        SavingGroup group = groupRepository.findByGroupId(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));

        // Verify requester is group creator
        if (!group.getCreatorId().equals(requestedBy)) {
            throw new UnauthorizedGroupOperationException("Only group creator can update payout order");
        }

        if (group.getPayoutOrderType() != SavingGroup.PayoutOrderType.CREATOR_DEFINED) {
            throw new InvalidPayoutOrderException("Payout order can only be updated for groups with CREATOR_DEFINED payout type");
        }

        // Validate all member IDs are present
        List<GroupMember> currentMembers = memberRepository.findByGroupGroupIdAndStatusOrderByPayoutOrderAsc(groupId, GroupMember.MemberStatus.ACTIVE);
        if (request.getMemberIds().size() != currentMembers.size()) {
            throw new InvalidPayoutOrderException("Payout order must include all active members");
        }

        Set<String> currentMemberIds = currentMembers.stream()
                .map(GroupMember::getUserId)
                .collect(Collectors.toSet());

        if (!currentMemberIds.equals(new HashSet<>(request.getMemberIds()))) {
            throw new InvalidPayoutOrderException("Payout order includes invalid member IDs");
        }

        // Update payout order
        Map<String, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < request.getMemberIds().size(); i++) {
            orderMap.put(request.getMemberIds().get(i), i + 1);
        }

        currentMembers.forEach(member ->
                member.setPayoutOrder(orderMap.get(member.getUserId())));

        memberRepository.saveAll(currentMembers);
    }

    @Override
    public GroupSummaryResponse getGroupSummary(String groupId) {
        return null;
    }

    @Override
    @Scheduled(cron = "0 */1 * * * *") //
    public void processScheduledContributions() {
        log.info("++++++++++Processing scheduled contribution++++++++++");
        LocalDateTime now = LocalDateTime.now();
        List<SavingGroup> dueGroups = groupRepository.findByNextContributionDateBeforeAndStatus(now, SavingGroup.GroupStatus.ACTIVE);

        dueGroups.forEach(group -> {
            try {
                processGroupContribution(group);
            } catch (Exception e) {
                log.error("Failed to process contributions for group {}: {}", group.getGroupId(), e.getMessage());
            }
        });
    }

    private void processGroupContribution(SavingGroup group) {
        List<GroupMember> activeMembers = memberRepository.findByGroupGroupIdAndStatusOrderByPayoutOrderAsc(
                group.getGroupId(), GroupMember.MemberStatus.ACTIVE);

        GroupMember recipient = determineRecipient(group, activeMembers);
        log.info("Recipient of contribution: {}", recipient.getUserId());

        String recipientWalletId = walletService.getWalletBalanceByUserId(recipient.getUserId()).getWalletId();

        List<ContributionDetail> contributionDetails = new ArrayList<>();

        for (GroupMember member : activeMembers) {
            if (member.getUserId().equals(recipient.getUserId())) {
                continue; // Skip recipient
            }

            String sourceWalletId = walletService.getWalletBalanceByUserId(member.getUserId()).getWalletId();

            try {
                // Transfer funds using WalletService
                TransactionResponse transferResponse = walletService.transferFunds(
                        new TransferRequest(
                                sourceWalletId,
                                recipientWalletId,
                                group.getContributionAmount(),
                                "GROUP_CONTRIBUTION_" + group.getGroupId(),
                                "SYSTEM"
                        )
                );

                // Record contribution
                GroupContribution contribution = GroupContribution.builder()
                        .contributionId(UUID.randomUUID().toString())
                        .group(group)
                        .member(member)
                        .amount(group.getContributionAmount())
                        .contributionDate(LocalDateTime.now())
                        .status(GroupContribution.ContributionStatus.COMPLETED)
                        .transactionReference(transferResponse.getTransactionId())
                        .build();

                contributionRepository.save(contribution);
                contributionDetails.add(new ContributionDetail(
                        member.getUserId(),
                        group.getContributionAmount(),
                        true,
                        null
                ));
            } catch (Exception e) {
                contributionDetails.add(new ContributionDetail(
                        member.getUserId(),
                        group.getContributionAmount(),
                        false,
                        e.getMessage()
                ));
                log.error("Failed to process contribution for member {}: {}",
                        member.getUserId(), e.getMessage());
            }
        }

        // Update group for next contribution
        group.setNextContributionDate(calculateNextContributionDate(group.getFrequency()));
        if (group.getType() == SavingGroup.GroupType.ROTATING) {
            group.setCurrentRound(group.getCurrentRound() + 1);
        }
        groupRepository.save(group);

        // Log contribution results
        logContributions(group, contributionDetails);
    }

    private void logContributions(SavingGroup group, List<ContributionDetail> details) {
        long successCount = details.stream().filter(ContributionDetail::isSuccess).count();
        log.info("Processed contributions for group {}: {}/{} successful",
                group.getName(), successCount, details.size());

        details.stream()
                .filter(d -> !d.isSuccess())
                .forEach(d -> log.warn("Failed contribution for member {}: {}",
                        d.getMemberId(), d.getErrorMessage()));
    }


    // Helper methods
    private void validateMemberCanJoin(SavingGroup group, String userId, BigDecimal minimumAmount) {
        // Check wallet balance
        WalletResponse userWalletResponse = walletService.getWalletBalanceByUserId(userId);
        BigDecimal userBalance = userWalletResponse.getBalance();
        if (userBalance.compareTo(minimumAmount) < 0) {
            throw new InsufficientJoinAmountException(
                    String.format("User must have at least %s in their wallet to join. Current balance: %s",
                            minimumAmount, userBalance));
        }

        // Check if group can accept new members
        if (!group.canAcceptNewMembers()) {
            throw new GroupNotAcceptingMembersException("Group is not currently accepting new members");
        }

        // Check if group is full
        if (group.isFull()) {
            throw new GroupFullException("Group has reached maximum number of participants");
        }

        // Check if user is already a member
        if (memberRepository.findByGroupGroupIdAndUserId(group.getGroupId(), userId).isPresent()) {
            throw new AlreadyGroupMemberException("User is already a member of this group");
        }
    }

    private GroupMember determineRecipient(SavingGroup group, List<GroupMember> members) {
        switch (group.getType()) {
            case ROTATING:
                int recipientIndex = (group.getCurrentRound() - 1) % members.size();
                return members.get(recipientIndex);
            case FIXED_DEPOSIT:
            case TARGET_SAVING:
                return members.stream()
                        .filter(m -> m.getUserId().equals(group.getCreatorId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Creator not found in group members"));
            default:
                throw new UnsupportedOperationException("Group type not supported for automatic contributions");
        }
    }

    private void initializePayoutOrder(SavingGroup group) {
        if (group.getPayoutOrderType() == SavingGroup.PayoutOrderType.RANDOM) {
            List<GroupMember> members = memberRepository.findByGroupGroupIdAndStatusOrderByPayoutOrderAsc(
                    group.getGroupId(), GroupMember.MemberStatus.ACTIVE);
            Collections.shuffle(members);

            for (int i = 0; i < members.size(); i++) {
                members.get(i).setPayoutOrder(i + 1);
            }

            memberRepository.saveAll(members);
        }
    }

    private LocalDateTime calculateNextContributionDate(SavingGroup.ContributionFrequency frequency) {
        LocalDateTime now = LocalDateTime.now();
        switch (frequency) {
            case TWO_MINUTES: return now.plusMinutes(2);
            case DAILY: return now.plusDays(1);
            case WEEKLY: return now.plusWeeks(1);
            case BI_WEEKLY: return now.plusWeeks(2);
            case MONTHLY: return now.plusMonths(1);
            default: throw new IllegalArgumentException("Unknown contribution frequency: " + frequency);
        }
    }

    // Mapping methods
    private GroupResponse mapToGroupResponse(SavingGroup group) {
        return GroupResponse.builder()
                .groupId(group.getGroupId())
                .name(group.getName())
                .description(group.getDescription())
                .creatorId(group.getCreatorId())
                .type(group.getType().name())
                .minimumContribution(group.getMinimumContribution())
                .minimumJoinAmount(group.getMinimumJoinAmount())
                .maxParticipants(group.getMaxParticipants())
                .contributionAmount(group.getContributionAmount())
                .frequency(group.getFrequency().name())
                .payoutOrderType(group.getPayoutOrderType().name())
                .status(group.getStatus().name())
                .nextContributionDate(group.getNextContributionDate())
                .currentRound(group.getCurrentRound())
                .targetAmount(group.getTargetAmount())
                .memberCount(memberRepository.countActiveMembersByGroupId(group.getGroupId()))
                .createdAt(group.getCreatedAt())
                .build();
    }

    private GroupMemberResponse mapToGroupMemberResponse(GroupMember member) {
        return GroupMemberResponse.builder()
                .memberId(member.getId())
                .groupId(member.getGroup().getGroupId())
                .userId(member.getUserId())
                .payoutOrder(member.getPayoutOrder())
                .status(member.getStatus().name())
                .joinDate(member.getJoinDate())
                .createdAt(member.getCreatedAt())
                .build();
    }

    private GroupInviteResponse mapToGroupInviteResponse(GroupInvite invite) {
        return GroupInviteResponse.builder()
                .inviteId(invite.getId())
                .groupId(invite.getGroup().getGroupId())
                .inviteCode(invite.getInviteCode())
                .invitedUserId(invite.getInvitedUserId())
                .invitedByUserId(invite.getInvitedByUserId())
                .minimumAmountRequired(invite.getMinimumAmountRequired())
                .status(invite.getStatus().name())
                .expiryDate(invite.getExpiryDate())
                .createdAt(invite.getCreatedAt())
                .build();
    }

    private GroupContributionResponse mapToGroupContributionResponse(GroupContribution contribution) {
        return GroupContributionResponse.builder()
                .contributionId(contribution.getContributionId())
                .groupId(contribution.getGroup().getGroupId())
                .memberId(contribution.getMember().getUserId())
                .amount(contribution.getAmount())
                .contributionDate(contribution.getContributionDate())
                .status(contribution.getStatus().name())
                .transactionReference(contribution.getTransactionReference())
                .createdAt(contribution.getCreatedAt())
                .build();
    }

    private GroupSummaryResponse mapToGroupSummaryResponse(SavingGroup group, List<GroupContribution> contributions) {
        BigDecimal totalContributions = contributions.stream()
                .map(GroupContribution::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return GroupSummaryResponse.builder()
                .groupId(group.getGroupId())
                .totalContributions(totalContributions)
                .totalMembers(memberRepository.countActiveMembersByGroupId(group.getGroupId()))
                .nextContributionDate(group.getNextContributionDate())
                .build();
    }
}
