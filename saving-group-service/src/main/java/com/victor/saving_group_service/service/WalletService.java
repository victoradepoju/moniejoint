package com.victor.saving_group_service.service;

import com.victor.saving_group_service.dto.request.TransferRequest;
import com.victor.saving_group_service.dto.response.TransactionResponse;
import com.victor.saving_group_service.dto.response.WalletResponse;

import java.math.BigDecimal;

public interface WalletService {
    WalletResponse getWalletBalanceByUserId(String userId);
    TransactionResponse transferFunds(TransferRequest request);
}
