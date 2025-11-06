package org.example.logistics.service;

import org.example.logistics.dto.inventory.OutboundCreateDto;
import org.example.logistics.dto.inventory.OutboundResponseDto;
import org.example.logistics.entity.Inventory;
import org.example.logistics.entity.InventoryMovement;
import org.example.logistics.mapper.OutboundMapper;
import org.example.logistics.repository.InventoryRepository;
import org.example.logistics.repository.InventoryMovementRepository;
import org.example.logistics.repository.ProductRepository;
import org.example.logistics.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class OutboundService {
    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private OutboundMapper outboundMapper;


    public OutboundResponseDto recordOutbound(OutboundCreateDto dto) {
        // Vérif produit/entrepôt
        if (!productRepository.existsById(dto.getProductId())) {
            throw new RuntimeException("Produit non trouvé : ID " + dto.getProductId());
        }
        if (!warehouseRepository.existsById(dto.getWarehouseId())) {
            throw new RuntimeException("Entrepôt non trouvé : ID " + dto.getWarehouseId());
        }

        if (dto.getQuantity() <= 0) {
            throw new RuntimeException("Quantité doit être positive");
        }

        // Find inventory
        Optional<Inventory> optInv = inventoryRepository.findByProductIdAndWarehouseId(
                dto.getProductId(), dto.getWarehouseId());
        if (optInv.isEmpty()) {
            throw new RuntimeException("Inventaire non trouvé pour produit " + dto.getProductId() + " dans entrepôt " + dto.getWarehouseId());
        }

        Inventory inv = optInv.get();
        int available = inv.getAvailable();
        if (available < dto.getQuantity()) {
            throw new RuntimeException("Stock insuffisant : Disponible " + available + ", demandé " + dto.getQuantity());
        }

        // Update qtyOnHand
        inv.setQtyOnHand(inv.getQtyOnHand() - dto.getQuantity());

        // Save inventory
        Inventory updatedInv = inventoryRepository.save(inv);

        // créer mouvement OUTBOUND
        InventoryMovement movement = outboundMapper.toMovement(dto);
        movement.setProduct(updatedInv.getProduct());
        movement.setWarehouse(updatedInv.getWarehouse());
        inventoryMovementRepository.save(movement);

        // DTO response
        OutboundResponseDto response = outboundMapper.toDto(updatedInv, dto);
        response.setId(movement.getId());
        response.setOccurredAt(movement.getOccurredAt());
        response.setMessage("Sortie enregistrée pour " + dto.getQuantity() + " unités");

        return response;
    }
}