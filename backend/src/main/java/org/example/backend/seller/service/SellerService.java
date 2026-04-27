package org.example.backend.seller.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.backend.seller.dto.SellerDataDto;
import org.example.backend.shop.model.Shop;
import org.example.backend.user.model.Seller;
import org.example.backend.user.repository.UserRepository;
import org.example.backend.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final UserRepository userRepository;
    private final UserService userService;

    public boolean linkShopToSeller(Seller seller, Shop shop) {
        if(seller.getShopId() != null) {
            return false; //Seller already has a shop linked
        }

        seller.setShopId(shop.getId());
        seller.setUpdatedAt(LocalDateTime.now());
        userRepository.save(seller);
        return true;
    }

    public SellerDataDto getCurrentSellerData(){
        Seller currentSeller = userService.getCurrentSeller();
        return SellerDataDto.from(currentSeller);
    }

    public SellerDataDto setSellerData(SellerDataDto sellerDataDto) {
        Seller currentSeller = userService.getCurrentSeller();
        currentSeller.setBusinessName(sellerDataDto.businessName());
        currentSeller.setAddress(sellerDataDto.address());
        currentSeller.setBillingAddress(sellerDataDto.billingAddress());
        currentSeller.setTaxId(sellerDataDto.taxId());
        currentSeller.setUpdatedAt(LocalDateTime.now());
        userRepository.save(currentSeller);
        return SellerDataDto.from(currentSeller);
    }
}
