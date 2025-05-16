package com.victor.user_service.client;

import com.victor.user_service.dto.CreateWalletRequest;
import com.victor.user_service.dto.WalletResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wallet-service", path = "/v1/wallets")
public interface WalletServiceClient {
    @PostMapping
    WalletResponse createWallet(@RequestBody CreateWalletRequest request);
}
