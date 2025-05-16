package com.victor.saving_group_service.client;

import com.victor.saving_group_service.config.FeignClientConfig;
import com.victor.saving_group_service.dto.request.TransferRequest;
import com.victor.saving_group_service.dto.response.TransactionResponse;
import com.victor.saving_group_service.dto.response.WalletResponse;
import com.victor.saving_group_service.service.WalletService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@Service
@FeignClient(
        name = "wallet-service",
        url = "${wallet.service.url:http://localhost:8007}",
        configuration = FeignClientConfig.class
)
public interface WalletServiceClient extends WalletService {

    @Override
    @GetMapping("/v1/wallets/user/{userId}")
    WalletResponse getWalletBalanceByUserId(@PathVariable String userId);

    @Override
    @PostMapping("/v1/wallets/transfer")
    TransactionResponse transferFunds(@RequestBody TransferRequest request);
}
