package org.example.backend.shop.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.seller.service.SellerService;
import org.example.backend.shop.dto.CreateShopRequest;
import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.shop.dto.UpdateShopRequest;
import org.example.backend.shop.model.Shop;
import org.example.backend.shop.model.ShopStatus;
import org.example.backend.shop.repository.ShopRepository;
import org.example.backend.user.model.Seller;
import org.example.backend.user.service.UserService;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final SellerService sellerService;
    private final UserService userService;

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

    /* ------------- HELPER METHODS -------------*/

    private Shop getCurrentSellerShopEntity(){
        Seller currentSeller = userService.getCurrentSeller();

        return shopRepository.findBySellerId(currentSeller.getId())
                .orElseThrow(() -> new IllegalStateException("Der Shop des Händlers wurde nicht gefunden."));
    }

    private String createSlug(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein.");
        }

        // NFD normalisation decomposes characters like ä → a + combining diacritic
        String normalized = Normalizer.normalize(name.toLowerCase().trim(), Normalizer.Form.NFD);

        StringBuilder slug = new StringBuilder(normalized.length());
        boolean prevDash = false;

        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            int type = Character.getType(c);

            // Drop combining/diacritic marks produced by NFD decomposition
            if (type == Character.NON_SPACING_MARK
                    || type == Character.COMBINING_SPACING_MARK
                    || type == Character.ENCLOSING_MARK) {
                continue;
            }

            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                slug.append(c);
                prevDash = false;
            } else if (!prevDash && !slug.isEmpty()) {
                // Collapse any run of non-alphanumeric characters into a single dash
                slug.append('-');
                prevDash = true;
            }
        }

        // Remove trailing dash
        int end = slug.length();
        while (end > 0 && slug.charAt(end - 1) == '-') {
            end--;
        }

        return slug.substring(0, end);
    }

}
