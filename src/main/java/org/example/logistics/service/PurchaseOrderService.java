package org.example.logistics.service;

import org.example.logistics.dto.purchase.PurchaseOrderCreateDto;
import org.example.logistics.dto.purchase.PurchaseOrderReceiveDto;
import org.example.logistics.dto.purchase.PurchaseOrderResponseDto;
import org.example.logistics.entity.*;
import org.example.logistics.entity.Enum.MovementType;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.mapper.PurchaseOrderMapper;
import org.example.logistics.repository.InventoryMovementRepository;
import org.example.logistics.repository.InventoryRepository;
import org.example.logistics.repository.ProductRepository;
import org.example.logistics.repository.PurchaseOrderLineRepository;
import org.example.logistics.repository.PurchaseOrderRepository;
import org.example.logistics.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Transactional
public class
PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PurchaseOrderLineRepository purchaseOrderLineRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Transactional
    public PurchaseOrderResponseDto createPurchaseOrder(PurchaseOrderCreateDto dto) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé : ID " + dto.getSupplierId()));

        PurchaseOrder po = PurchaseOrder.builder()
                .supplier(supplier)
                .status(Status.APPROVED)
                .build();

        List<PurchaseOrderLine> lines = dto.getLines().stream()
                .map(lineDto -> {
                    Product product = productRepository.findById(lineDto.getProductId())
                            .orElseThrow(() -> new RuntimeException("Produit non trouvé : ID " + lineDto.getProductId()));

                    return PurchaseOrderLine.builder()
                            .purchaseOrder(po)
                            .product(product)
                            .quantity(lineDto.getQuantity())
                            .receivedQty(0)
                            .build();
                })
                .collect(Collectors.toList());

        po.setLines(lines);

        PurchaseOrder saved = purchaseOrderRepository.save(po);

        return purchaseOrderMapper.toDto(saved);
    }

    private List<PurchaseOrderLine> createLines(PurchaseOrder po, List<PurchaseOrderCreateDto.LineCreateDto> lineDtos) {
        return lineDtos.stream()
                .map(lineDto -> {
                    Product product = productRepository.findById(lineDto.getProductId())
                            .orElseThrow(() -> new RuntimeException("Produit non trouvé : ID " + lineDto.getProductId()));

                    PurchaseOrderLine line = PurchaseOrderLine.builder()
                            .purchaseOrder(po)
                            .product(product)
                            .quantity(lineDto.getQuantity())
                            .receivedQty(0)
                            .build();

                    line.setPurchaseOrder(po);
                    line.setProduct(product);
                    return line;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public PurchaseOrderResponseDto receivePurchaseOrder(Long poId, PurchaseOrderReceiveDto dto) {
        Optional<PurchaseOrder> optPo = purchaseOrderRepository.findByIdAndStatus(poId, Status.APPROVED);
        if (optPo.isEmpty()) {
            throw new RuntimeException("PO non trouvé ou non approuvé");
        }

        PurchaseOrder po = optPo.get();

        for (int i = 0; i < po.getLines().size(); i++) {
            PurchaseOrderLine line = po.getLines().get(i);
            PurchaseOrderReceiveDto.LineReceiveDto receivedLine = dto.getLines().get(i);

            line.setReceivedQty(receivedLine.getReceivedQuantity());
            purchaseOrderLineRepository.save(line);

            receiveLineAndUpdateStock(line, po.getSupplier(), receivedLine.getReceivedQuantity());
        }

        po.setStatus(Status.RECEIVED);
        PurchaseOrder saved = purchaseOrderRepository.save(po);

        return purchaseOrderMapper.toDto(saved);
    }

   
    private void receiveLineAndUpdateStock(PurchaseOrderLine line, Supplier supplier, Integer receivedQuantity) {
        if (receivedQuantity > 0) {
            if (supplier.getWarehouse() == null) {
                throw new RuntimeException("Le fournisseur " + supplier.getName() + " n’a pas de warehouse associé !");
            }

            Optional<Inventory> optInv = inventoryRepository.findByProductIdAndWarehouseId(
                    line.getProduct().getId(),
                    supplier.getWarehouse().getId()
            );

            Inventory inv = optInv.orElseThrow(() ->
                    new RuntimeException("Inventaire non trouvé pour produit " + line.getProduct().getSku()));

            inv.setQtyOnHand(inv.getQtyOnHand() + receivedQuantity);
            inventoryRepository.save(inv);

            InventoryMovement movement = InventoryMovement.builder()
                    .product(line.getProduct())
                    .warehouse(inv.getWarehouse())
                    .type(MovementType.INBOUND)
                    .quantity(receivedQuantity)
                    .referenceDoc("PO" + line.getPurchaseOrder().getId())
                    .build();
            inventoryMovementRepository.save(movement);
        }
    }

}