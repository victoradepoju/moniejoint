package com.victor.wallet_service.service.impl;

import com.victor.wallet_service.dto.response.*;
import com.victor.wallet_service.dto.request.*;
import com.victor.wallet_service.exception.*;
import com.victor.wallet_service.model.Wallet;
import com.victor.wallet_service.model.WalletTransaction;
import com.victor.wallet_service.repository.WalletRepository;
import com.victor.wallet_service.repository.WalletTransactionRepository;
import com.victor.wallet_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    @Override
    public WalletResponse createWallet(CreateWalletRequest request) {
        if (walletRepository.existsByUserId(request.getUserId())) {
            throw new WalletAlreadyExistsException("Wallet already exists for user: " + request.getUserId());
        }

        Wallet wallet = Wallet.builder()
                .walletId(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .currency(request.getCurrency())
                .createdAt(LocalDateTime.now())
                .build();

        Wallet savedWallet = walletRepository.save(wallet);
        return mapToWalletResponse(savedWallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletByWalletId(String walletId) {
        Wallet wallet = walletRepository.findByWalletId(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with ID: " + walletId));
        return mapToWalletResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletByUserId(String userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));
        return mapToWalletResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getWalletBalance(String walletId) {
        Wallet wallet = walletRepository.findByWalletId(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with ID: " + walletId));
        return wallet.getBalance();
    }

    @Override
    public TransactionResponse depositFunds(DepositRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Deposit amount must be positive");
        }

        Wallet wallet = walletRepository.findByWalletIdWithLock(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with ID: " + request.getWalletId()));

        wallet.deposit(request.getAmount());
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        WalletTransaction transaction = createTransaction(
                wallet,
                request.getAmount(),
                WalletTransaction.TransactionType.DEPOSIT,
                request.getReference(),
                request.getDescription(),
                request.getInitiatedBy()
        );

        return mapToTransactionResponse(transaction);
    }

    @Override
    public TransactionResponse withdrawFunds(WithdrawRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be positive");
        }

        Wallet wallet = walletRepository.findByWalletIdWithLock(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with ID: " + request.getWalletId()));

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }

        wallet.withdraw(request.getAmount());
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        WalletTransaction transaction = createTransaction(
                wallet,
                request.getAmount(),
                WalletTransaction.TransactionType.WITHDRAWAL,
                request.getReference(),
                request.getDescription(),
                request.getInitiatedBy()
        );

        return mapToTransactionResponse(transaction);
    }

    @Override
    public TransferResponse transferFunds(TransferRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Transfer amount must be positive");
        }

        // Get wallets with lock (ordered by ID to prevent deadlocks)
        Wallet sourceWallet = walletRepository.findByWalletIdWithLock(request.getSourceWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Source wallet not found with ID: " + request.getSourceWalletId()));

        Wallet destWallet = walletRepository.findByWalletIdWithLock(request.getDestinationWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Destination wallet not found with ID: " + request.getDestinationWalletId()));

        if (!sourceWallet.getCurrency().equals(destWallet.getCurrency())) {
            throw new CurrencyMismatchException("Source and destination wallets must have the same currency");
        }

        if (sourceWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }

        // Perform transfer
        sourceWallet.withdraw(request.getAmount());
        sourceWallet.setUpdatedAt(LocalDateTime.now());
        destWallet.deposit(request.getAmount());
        destWallet.setUpdatedAt(LocalDateTime.now());

        walletRepository.saveAll(List.of(sourceWallet, destWallet));

        // Create transactions
        WalletTransaction debitTransaction = createTransaction(
                sourceWallet,
                request.getAmount(),
                WalletTransaction.TransactionType.TRANSFER,
                request.getReference(),
                "Transfer to " + destWallet.getWalletId(),
                request.getInitiatedBy()
        );

        WalletTransaction creditTransaction = createTransaction(
                destWallet,
                request.getAmount(),
                WalletTransaction.TransactionType.TRANSFER,
                request.getReference(),
                "Transfer from " + sourceWallet.getWalletId(),
                request.getInitiatedBy()
        );

        return new TransferResponse(
                mapToTransactionResponse(debitTransaction),
                mapToTransactionResponse(creditTransaction)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getWalletTransactions(String walletId) {
        walletRepository.findByWalletId(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with ID: " + walletId));

        return transactionRepository.findByWalletWalletId(walletId).stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    private WalletTransaction createTransaction(Wallet wallet, BigDecimal amount,
                                                WalletTransaction.TransactionType type, String reference,
                                                String description, String initiatedBy) {
        WalletTransaction transaction = WalletTransaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .wallet(wallet)
                .amount(amount)
                .currency(wallet.getCurrency())
                .type(type)
                .status(WalletTransaction.TransactionStatus.COMPLETED)
                .reference(reference)
                .description(description)
                .initiatedBy(initiatedBy)
                .build();

        return transactionRepository.save(transaction);
    }

    private WalletResponse mapToWalletResponse(Wallet wallet) {
        return WalletResponse.builder()
                .walletId(wallet.getWalletId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus().name())
                .createdAt(wallet.getCreatedAt())
                .build();
    }

    private TransactionResponse mapToTransactionResponse(WalletTransaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .walletId(transaction.getWallet().getWalletId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .type(transaction.getType().name())
                .status(transaction.getStatus().name())
                .reference(transaction.getReference())
                .description(transaction.getDescription())
                .initiatedBy(transaction.getInitiatedBy())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
