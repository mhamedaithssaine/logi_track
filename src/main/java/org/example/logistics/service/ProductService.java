package org.example.logistics.service;

import org.example.logistics.dto.product.ProductResponseDto;
import org.example.logistics.entity.Product;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.ProductMapper;
import org.example.logistics.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductMapper productMapper;

    //get Product by SKU

    @Transactional(readOnly = true)
    public ProductResponseDto getBySku(String sku){
        Product product = productRepository.findBySku(sku)
                .orElseThrow(()-> ResourceNotFoundException.withString("Product", "Sku", sku));
        ProductResponseDto dto = productMapper.toDto(product);
        boolean available = Boolean.TRUE.equals(product.getActive());


        return dto;
    }
}
