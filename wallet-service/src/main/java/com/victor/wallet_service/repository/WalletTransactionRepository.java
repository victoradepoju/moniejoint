package com.victor.wallet_service.repository;

import com.victor.wallet_service.model.WalletTransaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByWalletWalletId(String walletId);
    Optional<WalletTransaction> findByTransactionId(String transactionId);

    @Query("SELECT t FROM WalletTransaction t WHERE t.wallet.walletId = :walletId " +
            "AND t.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY t.createdAt DESC")
    List<WalletTransaction> findByWalletAndDateRange(
            String walletId,
            LocalDateTime startDate,
            LocalDateTime endDate);
}
