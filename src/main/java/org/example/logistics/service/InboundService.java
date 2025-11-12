package org.example.logistics.service;

import org.example.logistics.dto.inventory.InboundCreateDto;
import org.example.logistics.dto.inventory.InboundResponseDto;
import org.example.logistics.entity.Inventory;
import org.example.logistics.entity.InventoryMovement;
import org.example.logistics.mapper.InboundMapper;
import org.example.logistics.repository.InventoryRepository;
import org.example.logistics.repository.InventoryMovementRepository;
import org.example.logistics.repository.ProductRepository;
import org.example.logistics.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InboundService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private InboundMapper inboundMapper;


    public InboundResponseDto recordInbound(InboundCreateDto dto) {
        var product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé : ID " + dto.getProductId()));

        var warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé : ID " + dto.getWarehouseId()));

        if (dto.getQuantity() <= 0) {
            throw new RuntimeException("Quantité doit être positive");
        }

        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(dto.getProductId(), dto.getWarehouseId())
                .orElseGet(() -> inventoryRepository.save(
                        Inventory.builder()
                                .product(product)
                                .warehouse(warehouse)
                                .qtyOnHand(0)
                                .qtyReserved(0)
                                .build()
                ));

        inv.setQtyOnHand(inv.getQtyOnHand() + dto.getQuantity());
        Inventory updatedInv = inventoryRepository.save(inv);

        InventoryMovement movement = inboundMapper.toMovement(dto);
        movement.setProduct(updatedInv.getProduct());
        movement.setWarehouse(updatedInv.getWarehouse());
        inventoryMovementRepository.save(movement);

        return InboundResponseDto.builder()
                .id(movement.getId())
                .productId(updatedInv.getProduct().getId())
                .warehouseId(updatedInv.getWarehouse().getId())
                .quantityAdded(dto.getQuantity())
                .newQtyOnHand(updatedInv.getQtyOnHand().doubleValue())
                .occurredAt(movement.getOccurredAt())
                .message("Réception enregistrée pour " + dto.getQuantity() + " unités")
                .build();
    }
}
