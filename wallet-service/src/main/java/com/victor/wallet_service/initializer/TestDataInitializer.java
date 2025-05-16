package com.victor.wallet_service.initializer;

import com.victor.wallet_service.model.Wallet;
import com.victor.wallet_service.model.WalletTransaction;
import com.victor.wallet_service.repository.WalletRepository;
import com.victor.wallet_service.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataInitializer implements CommandLineRunner {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    @Override
    public void run(String... args) {
        // Create test wallets
        Wallet wallet1 = Wallet.builder()
                .walletId("wallet-1")
                .userId("user-1")
                .balance(new BigDecimal("1000"))
                .currency("NGN")
                .build();

        Wallet wallet2 = Wallet.builder()
                .walletId("wallet-2")
                .userId("user-2")
                .balance(new BigDecimal("500"))
                .currency("NGN")
                .build();

        walletRepository.saveAll(List.of(wallet1, wallet2));

        // Create test transactions
        WalletTransaction deposit = WalletTransaction.builder()
                .transactionId("txn-1")
                .wallet(wallet1)
                .amount(new BigDecimal("1000"))
                .currency("NGN")
                .type(WalletTransaction.TransactionType.DEPOSIT)
                .status(WalletTransaction.TransactionStatus.COMPLETED)
                .initiatedBy("system")
                .build();

        transactionRepository.save(deposit);
    }
}
