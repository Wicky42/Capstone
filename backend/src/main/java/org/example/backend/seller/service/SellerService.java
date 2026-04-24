package org.example.backend.seller.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.backend.shop.model.Shop;
import org.example.backend.user.model.Seller;
import org.example.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final UserRepository userRepository;

    public boolean linkShopToSeller(Seller seller, Shop shop) {
        if(seller.getShopId() != null) {
            return false; //Seller already has a shop linked
        }

        seller.setShopId(shop.getId());
        seller.setUpdatedAt(LocalDateTime.now());
        userRepository.save(seller);
        return true;
    }
}
