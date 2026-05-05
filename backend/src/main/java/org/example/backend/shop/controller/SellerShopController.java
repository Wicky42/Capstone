package org.example.backend.shop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.shop.dto.CreateShopRequest;
import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.shop.dto.UpdateShopRequest;
import org.example.backend.shop.service.ShopService;
import org.example.backend.user.model.Seller;
import org.example.backend.user.service.UserService;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/shops")
@RequiredArgsConstructor
public class SellerShopController {

    private final ShopService shopService;
    private final UserService userService;

    @PostMapping
    public ShopResponse createNewShop(@Valid @RequestBody CreateShopRequest request){
        Seller currentSeller = userService.getCurrentSeller();
        return shopService.createShopForSeller(currentSeller, request);
    }

    @GetMapping("/my")
    public ShopResponse getMyShop(){
        return shopService.getCurrentSellerShop();
    }

    @PutMapping("/my")
    public ShopResponse updateMyShop(@Valid @RequestBody UpdateShopRequest request) {
        return shopService.updateCurrentSellerShop(request);
    }
}
