package org.example.logistics.service;

import org.example.logistics.dto.inventory.AdjustmentCreateDto;
import org.example.logistics.dto.inventory.AdjustmentResponseDto;
import org.example.logistics.entity.Inventory;
import org.example.logistics.entity.InventoryMovement;
import org.example.logistics.mapper.AdjustmentMapper;
import org.example.logistics.repository.InventoryRepository;
import org.example.logistics.repository.InventoryMovementRepository;
import org.example.logistics.repository.ProductRepository;
import org.example.logistics.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AdjustmentService {
    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private AdjustmentMapper adjustmentMapper;


    public AdjustmentResponseDto adjustStock(AdjustmentCreateDto dto) {
        // Vérif produit/entrepôt
        if (!productRepository.existsById(dto.getProductId())) {
            throw new RuntimeException("Produit non trouvé : ID " + dto.getProductId());
        }
        if (!warehouseRepository.existsById(dto.getWarehouseId())) {
            throw new RuntimeException("Entrepôt non trouvé : ID " + dto.getWarehouseId());
        }

        // Find inventory
        Optional<Inventory> optInv = inventoryRepository.findByProductIdAndWarehouseId(
                dto.getProductId(), dto.getWarehouseId());
        if (optInv.isEmpty()) {
            throw new RuntimeException("Inventaire non trouvé pour produit " + dto.getProductId() + " dans entrepôt " + dto.getWarehouseId());
        }

        Inventory inv = optInv.get();
        int adjustment = dto.getAdjustmentQty();
        if (adjustment < 0) {
            // US8 : Négatif → Vérif stock >= reserved + |adjustment|
            int absoluteAdjustment = Math.abs(adjustment);
            if (inv.getQtyOnHand() < inv.getQtyReserved() + absoluteAdjustment) {
                throw new RuntimeException("Ajustement négatif refusé : Stock insuffisant pour réservation");
            }
        }

        // Update qtyOnHand += adjustment (positif/négatif)
        inv.setQtyOnHand(inv.getQtyOnHand() + adjustment);

        // Save inventory
        Inventory updatedInv = inventoryRepository.save(inv);

        // Créer mouvement ADJUSTMENT
        InventoryMovement movement = adjustmentMapper.toMovement(dto);
        movement.setProduct(updatedInv.getProduct());
        movement.setWarehouse(updatedInv.getWarehouse());
        inventoryMovementRepository.save(movement);

        // DTO réponse
        AdjustmentResponseDto response = adjustmentMapper.toDto(updatedInv, dto);
        response.setId(movement.getId());
        response.setOccurredAt(movement.getOccurredAt());
        response.setMessage("Ajustement enregistré pour " + adjustment + " unités");

        return response;
    }
}