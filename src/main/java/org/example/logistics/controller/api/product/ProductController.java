package org.example.logistics.controller.api.product;

import jakarta.validation.Valid;
import org.example.logistics.dto.product.ProductCreateDto;
import org.example.logistics.dto.product.ProductResponseDto;
import org.example.logistics.dto.product.ProductUpdateDto;
import org.example.logistics.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    //CREATE
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductCreateDto dto) {
        ProductResponseDto response = productService.createProduct(dto);
        return ResponseEntity.ok(response);
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        List<ProductResponseDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }


    // READ BY SKU
    @GetMapping("/{sku}")
    public ResponseEntity<ProductResponseDto> getProductBySku(@PathVariable String sku) {
        ProductResponseDto response = productService.getBySku(sku);
        return ResponseEntity.ok(response);
    }

    // UPDATE
    @PutMapping("/{sku}")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable String sku, @Valid @RequestBody ProductUpdateDto dto) {
        ProductResponseDto response = productService.updateProduct(sku, dto);
        return ResponseEntity.ok(response);
    }

    // DELETE
    @DeleteMapping("/{sku}")
    public ResponseEntity<String> deleteProduct(@PathVariable String sku) {
        String message = productService.deleteProduct(sku);
        return ResponseEntity.ok(message);
    }


    @PatchMapping("/{sku}/deactivate")
    public ResponseEntity<ProductResponseDto> deactivateProduct(@PathVariable String sku){
        ProductResponseDto responseDto = productService.descativeProduit(sku);
        return ResponseEntity.ok(responseDto);
    }

}
