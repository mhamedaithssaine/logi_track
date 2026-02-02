package org.example.logistics.controller.api.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        List<ProductResponseDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/catalogue")
    public ResponseEntity<List<ProductResponseDto>> getCatalogue(
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String searchBy,
            @RequestParam(required = false) String category) {
        List<ProductResponseDto> products = productService.getCatalogue(active, search, searchBy, category);
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
    public ResponseEntity<ProductResponseDto> deactivateProduct(@PathVariable @NotBlank String sku){
        ProductResponseDto response = productService.deactivateProduct(sku);
        return ResponseEntity.ok(response);
    }

}
