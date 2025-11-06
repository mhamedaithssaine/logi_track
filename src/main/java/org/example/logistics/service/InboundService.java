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
        if (!productRepository.existsById(dto.getProductId())) {
            throw new RuntimeException("Produit non trouvé : ID " + dto.getProductId());
        }
        if (!warehouseRepository.existsById(dto.getWarehouseId())) {
            throw new RuntimeException("Entrepôt non trouvé : ID " + dto.getWarehouseId());
        }

        if (dto.getQuantity() <= 0) {
            throw new RuntimeException("Quantité doit être positive");
        }

        Optional<Inventory> optInv = inventoryRepository.findByProductIdAndWarehouseId(
                dto.getProductId(), dto.getWarehouseId());
        Inventory inv = optInv.orElseGet(() -> {
            Inventory newInv = new Inventory();
            newInv.setProduct(productRepository.findById(dto.getProductId()).get());
            newInv.setWarehouse(warehouseRepository.findById(dto.getWarehouseId()).get());
            newInv.setQtyOnHand(0);
            newInv.setQtyReserved(0);
            return inventoryRepository.save(newInv);
        });

        inv.setQtyOnHand(inv.getQtyOnHand() + dto.getQuantity());

        Inventory updatedInv = inventoryRepository.save(inv);

        InventoryMovement movement = inboundMapper.toMovement(dto);
        movement.setProduct(updatedInv.getProduct());
        movement.setWarehouse(updatedInv.getWarehouse());
        inventoryMovementRepository.save(movement);

        InboundResponseDto response = inboundMapper.toDto(updatedInv);
        response.setId(movement.getId());
        response.setQuantityAdded(dto.getQuantity());
        response.setOccurredAt(movement.getOccurredAt());
        response.setMessage("Réception enregistrée pour " + dto.getQuantity() + " unités");
        return response;
    }
}