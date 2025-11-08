package org.example.logistics.service;

import org.example.logistics.dto.product.ProductCreateDto;
import org.example.logistics.dto.product.ProductResponseDto;
import org.example.logistics.dto.product.ProductUpdateDto;
import org.example.logistics.entity.Product;
import org.example.logistics.exception.ConflictException;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.ProductMapper;
import org.example.logistics.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductMapper productMapper;

    // Create
    @Transactional
    public ProductResponseDto createProduct(ProductCreateDto dto) {
        if (productRepository.existsBySku(dto.getSku())) {
            throw new RuntimeException("SKU existe déjà");
        }

        Product product = productMapper.toEntity(dto);
        Product saved = productRepository.save(product);

        return ProductResponseDto.builder()
                .id(saved.getId())
                .sku(saved.getSku())
                .name(saved.getName())
                .category(saved.getCategory())
                .price(saved.getPrice())
                .active(saved.getActive())
                .message("Produit créé avec succès")
                .build();
    }

    // Read All
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }


    //Get by SKU
    @Transactional(readOnly = true)
    public ProductResponseDto getBySku(String sku){
        Optional<Product> opt = productRepository.findBySku(sku);
        if (opt.isEmpty()) {
            throw  ResourceNotFoundException.withString("Produit", "SKU", sku);
        }

        Product product = opt.get();
        if (product.getName() == null || product.getCategory() == null || product.getPrice() == null) {
            System.out.println("Produit trouvé mais fields null : " + sku + " - Name: " + product.getName() + ", Category: " + product.getCategory() + ", Price: " + product.getPrice());  // Debug : Print fields
            throw new RuntimeException("Produit incomplet : champs null");
        }

        ProductResponseDto dto = productMapper.toDto(product);
        boolean available = Boolean.TRUE.equals(product.getActive());  // Safe : active not null

        return dto;
    }

    // Update
    @Transactional
    public ProductResponseDto updateProduct(String sku, ProductUpdateDto dto) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() ->  ResourceNotFoundException.withString("Produit", "SKU", sku));
        productMapper.toEntityFromUpdate(dto, product);
        Product saved = productRepository.save(product);

        return ProductResponseDto.builder()
                .id(saved.getId())
                .sku(saved.getSku())
                .name(saved.getName())
                .category(saved.getCategory())
                .price(saved.getPrice())
                .active(saved.getActive())
                .message("Produit mis à jour avec succès")
                .build();
    }

    // Delete
    @Transactional
    public String deleteProduct(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() ->  ResourceNotFoundException.withString("Produit", "SKU", sku));

        productRepository.delete(product);
        return "Produit supprimé avec succès";
    }


}
