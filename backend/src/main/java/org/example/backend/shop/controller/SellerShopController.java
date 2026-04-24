package org.example.backend.shop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.shop.dto.UpdateShopRequest;
import org.example.backend.shop.service.ShopService;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/shops")
@RequiredArgsConstructor
public class SellerShopController {

    private final ShopService shopService;

    @GetMapping("/my")
    public ShopResponse getMyShop(){
        return shopService.getCurrentSellerShop();
    }

    @PutMapping("/my")
    public ShopResponse updateMyShop(@Valid @RequestBody UpdateShopRequest request) {
        return shopService.updateCurrentSellerShop(request);
    }
}
