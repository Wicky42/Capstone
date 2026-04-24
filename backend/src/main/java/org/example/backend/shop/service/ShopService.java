package org.example.backend.shop.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.backend.seller.service.SellerService;
import org.example.backend.shop.dto.CreateShopRequest;
import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.shop.model.Shop;
import org.example.backend.shop.model.ShopStatus;
import org.example.backend.shop.repository.ShopRepository;
import org.example.backend.user.model.Seller;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final SellerService sellerService;

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

    /* ------------- HELPER METHODS -------------*/

    private String createSlug(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name darf nicht leer sein.");
        }

        return java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")          // Umlaute / Akzente vereinfachen
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")   // Sonderzeichen entfernen
                .replaceAll("\\s+", "-")           // Leerzeichen -> Bindestrich
                .replaceAll("-{2,}", "-")          // doppelte Bindestriche vermeiden
                .replaceAll("^-|-$", "");          // Bindestriche am Rand entfernen
    }

}
