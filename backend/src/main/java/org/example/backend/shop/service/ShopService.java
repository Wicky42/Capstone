package org.example.backend.shop.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.util.SlugUtils;
import org.example.backend.seller.service.SellerService;
import org.example.backend.shop.dto.CreateShopRequest;
import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.shop.dto.UpdateShopRequest;
import org.example.backend.shop.model.Shop;
import org.example.backend.shop.model.ShopStatus;
import org.example.backend.shop.repository.ShopRepository;
import org.example.backend.user.model.Seller;
import org.example.backend.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final SellerService sellerService;
    private final UserService userService;

    /*--------- Service functions for current seller
    if user is logged in as a seller, these functions will be used to create, update and get the shop of the current seller.
     */

    public ShopResponse createShopForSeller(Seller seller, CreateShopRequest request){
        //Shop with name already exists?
        if(shopRepository.existsByName(request.name())){
            throw new IllegalStateException("Ein Shop mit diesem Namen existiert bereits.");
        }

        //has Seller already Shop? -> throw RunTimeException("Händler hat bereits einen Shop.")
        if(seller.getShopId() != null || shopRepository.findBySellerId(seller.getId()).isPresent()){
            throw new IllegalStateException("Händler hat bereits einen Shop.");
        }
        //erstelle neuen Shop aus request mit init Daten und speichere diesen
        Shop shop = Shop.builder()
                .name(request.name())
                .slug(createSlug(request.name()))
                .description(request.description())
                .sellerId(seller.getId())
                .status(ShopStatus.DRAFT)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
        Shop savedShop = shopRepository.save(shop);

        //In SellerService linkToShop aufrufen
        if(!sellerService.linkShopToSeller(seller, savedShop)){
            throw new IllegalStateException("Fehler beim Verknüpfen von Shop und Händler.");
        }

        return ShopResponse.from(savedShop);
    }

    public ShopResponse getCurrentSellerShop(){
        return ShopResponse.from(getCurrentSellerShopEntity());
    }

    public ShopResponse updateCurrentSellerShop(UpdateShopRequest request) {
        Shop shop = getCurrentSellerShopEntity();

        boolean nameChanged = !shop.getName().equals(request.name());

        if (nameChanged && shopRepository.existsByName(request.name())) {
            throw new IllegalStateException("Ein Shop mit diesem Namen existiert bereits.");
        }

        shop.setName(request.name());
        shop.setDescription(request.description());
        shop.setLogoUrl(request.logoUrl());
        shop.setHeaderImageUrl(request.headerImageUrl());

        if (nameChanged) {
            shop.setSlug(createSlug(request.name()));
        }

        shop.setUpdatedAt(LocalDateTime.now());

        Shop savedShop = shopRepository.save(shop);

        return ShopResponse.from(savedShop);
    }

    /* ---------- Service functions for public shop functions

     */
    public List<String> getActiveShopIds(){
        return shopRepository.findByStatus(ShopStatus.ACTIVE).stream()
                .map(Shop::getId)
                .toList();
    }

    public boolean isShopActive(String shopId){
        return shopRepository.findById(shopId)
                .map(shop -> shop.getStatus() == ShopStatus.ACTIVE)
                .orElse(false);
    }

    public Page<ShopResponse> getActiveShops(Pageable pageable) {
        return shopRepository.findByStatus(ShopStatus.ACTIVE, pageable)
                .map(ShopResponse::from);
    }

    /* ------------- HELPER METHODS -------------*/

    private Shop getCurrentSellerShopEntity(){
        Seller currentSeller = userService.getCurrentSeller();

        return shopRepository.findBySellerId(currentSeller.getId())
                .orElseThrow(() -> new IllegalStateException("Der Shop des Händlers wurde nicht gefunden."));
    }

    private String createSlug(String name) {
        return SlugUtils.normalize(name);
    }

}
