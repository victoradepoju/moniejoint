package com.victor.wallet_service.controller;

import com.victor.wallet_service.dto.request.CreateWalletRequest;
import com.victor.wallet_service.dto.request.DepositRequest;
import com.victor.wallet_service.dto.request.TransferRequest;
import com.victor.wallet_service.dto.request.WithdrawRequest;
import com.victor.wallet_service.dto.response.TransactionResponse;
import com.victor.wallet_service.dto.response.TransferResponse;
import com.victor.wallet_service.dto.response.WalletResponse;
import com.victor.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@RequestBody @Valid CreateWalletRequest request) {
        log.info("Inside WalletController.createWallet with request: {}", request);
        WalletResponse response = walletService.createWallet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable String walletId) {
        log.info("Inside WalletController.getWallet with ID: {}", walletId);
        WalletResponse response = walletService.getWalletByWalletId(walletId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletResponse> getWalletByUserId(@PathVariable String userId) {
        log.info("Inside WalletController.getWalletByUserId with ID: {}", userId);
        WalletResponse response = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{walletId}/balance")
    public ResponseEntity<BigDecimal> getWalletBalance(@PathVariable String walletId) {
        log.info("Inside WalletController.getWalletBalance with wallet ID: {}", walletId);
        BigDecimal balance = walletService.getWalletBalance(walletId);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<TransactionResponse> depositFunds(
            @PathVariable String walletId,
            @RequestBody @Valid DepositRequest request)
    {
        log.info("Inside WalletController.depositFunds with request: {}", request);
        request.setWalletId(walletId);
        TransactionResponse response = walletService.depositFunds(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<TransactionResponse> withdrawFunds(
            @PathVariable String walletId,
            @RequestBody @Valid WithdrawRequest request)
    {
        log.info("Inside WalletController.withdrawFunds with request: {}", request);
        request.setWalletId(walletId);
        TransactionResponse response = walletService.withdrawFunds(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transferFunds(
            @RequestBody @Valid TransferRequest request)
    {
        log.info("Inside WalletController.transferFunds with request: {}", request);
        TransferResponse response = walletService.transferFunds(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getWalletTransactions(
            @PathVariable String walletId)
    {
        log.info("Inside WalletController.getWalletTransactions with ID: {}", walletId);
        List<TransactionResponse> responses = walletService.getWalletTransactions(walletId);
        return ResponseEntity.ok(responses);
    }
}
