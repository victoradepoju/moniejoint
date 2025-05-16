package com.victor.saving_group_service.controller;

import com.victor.saving_group_service.dto.request.*;
import com.victor.saving_group_service.dto.response.*;
import com.victor.saving_group_service.service.SavingGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/v1/groups")
@RequiredArgsConstructor
public class SavingGroupController {
    private final SavingGroupService savingGroupService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@RequestBody @Valid CreateGroupRequest request) {
        log.info("Inside SavingGroupController.createGroup with request: {}", request);
        GroupResponse response = savingGroupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable String groupId) {
        log.info("Inside SavingGroupController.getGroup with groupId: {}", groupId);
        GroupResponse response = savingGroupService.getGroup(groupId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GroupResponse>> getUserGroups(@PathVariable String userId) {
        log.info("Inside SavingGroupController.getUserGroups with userId: {}", userId);
        List<GroupResponse> responses = savingGroupService.getUserGroups(userId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<GroupMemberResponse> joinGroup(
            @PathVariable String groupId,
            @RequestBody @Valid JoinGroupRequest request)
    {
        log.info("Inside SavingGroupController.joinGroup with request: {} groupId: {}",request, groupId);
        GroupMemberResponse response = savingGroupService.joinGroup(groupId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/join/{inviteCode}")
    public ResponseEntity<GroupMemberResponse> joinGroupWithInvite(
            @PathVariable String inviteCode,
            @RequestBody @Valid JoinGroupRequest request)
    {
        log.info("Inside SavingGroupController.joinGroupWithInvite with request: {}, inviteCode: {}", request, inviteCode);
        GroupMemberResponse response = savingGroupService.joinGroupWithInvite(inviteCode, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{groupId}/invites")
    public ResponseEntity<GroupInviteResponse> createInvite(
            @PathVariable String groupId,
            @RequestBody @Valid CreateInviteRequest request)
    {
        log.info("Inside SavingGroupController.createInvite with request: {}, groupId: {}", request, groupId);
        GroupInviteResponse response = savingGroupService.createInvite(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{groupId}/invites")
    public ResponseEntity<List<GroupInviteResponse>> getGroupInvites(
            @PathVariable String groupId,
            @RequestParam String requestedBy)
    {
        log.info("Inside SavingGroupController.getGroupInvites with requestedBy: {}, groupId: {}", requestedBy, groupId);
        List<GroupInviteResponse> responses = savingGroupService.getGroupInvites(groupId, requestedBy);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/invites/user/{userId}")
    public ResponseEntity<List<GroupInviteResponse>> getUserInvites(@PathVariable String userId)
    {
        log.info("Inside SavingGroupController.getUserInvites with userId: {}", userId);
        List<GroupInviteResponse> responses = savingGroupService.getUserInvites(userId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{groupId}/contributions")
    public ResponseEntity<GroupContributionResponse> createContribution(
            @PathVariable String groupId,
            @RequestBody @Valid CreateContributionRequest request)
    {
        log.info("Inside SavingGroupController.createContribution with request: {}, groupId: {}", request, groupId);
        GroupContributionResponse response = savingGroupService.createContribution(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{groupId}/contributions")
    public ResponseEntity<List<GroupContributionResponse>> getGroupContributions(@PathVariable String groupId)
    {
        log.info("Inside SavingGroupController.getGroupContributions with groupId: {}", groupId);
        List<GroupContributionResponse> responses = savingGroupService.getGroupContributions(groupId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{groupId}/members/{userId}/contributions")
    public ResponseEntity<List<GroupContributionResponse>> getUserContributions(
            @PathVariable String groupId,
            @PathVariable String userId)
    {
        log.info("Inside SavingGroupController.getUserContributions with groupId: {}, userId: {}", groupId, userId);
        List<GroupContributionResponse> responses = savingGroupService.getUserContributions(groupId, userId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{groupId}/payout-order")
    public ResponseEntity<Void> updatePayoutOrder(
            @PathVariable String groupId,
            @RequestBody @Valid UpdatePayoutOrderRequest request,
            @RequestParam String requestedBy)
    {
        log.info("Inside SavingGroupController.updatePayoutOrder with request: {}, groupId: {}, requestedBy: {}", request, groupId, requestedBy);
        savingGroupService.updatePayoutOrder(groupId, request, requestedBy);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/summary")
    public ResponseEntity<GroupSummaryResponse> getGroupSummary(@PathVariable String groupId)
    {
        log.info("Inside SavingGroupController.getGroupSummary with groupId: {}", groupId);
        GroupSummaryResponse response = savingGroupService.getGroupSummary(groupId);
        return ResponseEntity.ok(response);
    }
}
