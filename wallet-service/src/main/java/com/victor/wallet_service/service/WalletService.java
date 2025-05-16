package com.victor.wallet_service.service;

import com.victor.wallet_service.dto.request.*;
import com.victor.wallet_service.dto.response.*;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    WalletResponse createWallet(CreateWalletRequest request);
    WalletResponse getWalletByWalletId(String walletId);
    WalletResponse getWalletByUserId(String userId);
    BigDecimal getWalletBalance(String walletId);
    TransactionResponse depositFunds(DepositRequest request);
    TransactionResponse withdrawFunds(WithdrawRequest request);
    TransferResponse transferFunds(TransferRequest request);
    List<TransactionResponse> getWalletTransactions(String walletId);
}
