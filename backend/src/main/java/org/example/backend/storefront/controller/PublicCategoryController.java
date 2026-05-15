package org.example.backend.storefront.controller;

import org.example.backend.product.model.ProductCategory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/categories")
public class PublicCategoryController {

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getCategories() {
        List<Map<String, String>> categories = Arrays.stream(ProductCategory.values())
                .map(cat -> Map.of("value", cat.name(), "label", cat.getLabel()))
                .toList();
        return ResponseEntity.ok(categories);
    }
}

