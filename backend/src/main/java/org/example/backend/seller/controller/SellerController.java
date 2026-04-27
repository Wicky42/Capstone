package org.example.backend.seller.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.seller.dto.SellerDataDto;
import org.example.backend.seller.service.SellerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/profile")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @GetMapping
    public SellerDataDto getSellerData() {
        return sellerService.getCurrentSellerData();
    }

    @PutMapping
    public SellerDataDto updateSellerData(@Valid @RequestBody SellerDataDto sellerDataDto) {
        return sellerService.setSellerData(sellerDataDto);
    }
}
