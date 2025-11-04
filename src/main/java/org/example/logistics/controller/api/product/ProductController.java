package org.example.logistics.controller.api.product;

import org.example.logistics.dto.product.ProductResponseDto;
import org.example.logistics.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/{sku}")
    public ResponseEntity<ProductResponseDto> getBySku(@PathVariable String sku){
        ProductResponseDto response = productService.getBySku(sku);
        return ResponseEntity.ok(response);
    }

}
