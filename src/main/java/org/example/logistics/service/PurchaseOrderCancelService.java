package org.example.logistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logistics.dto.purchase.PurchaseOrderCancelDto;
import org.example.logistics.dto.purchase.PurchaseOrderCancelResponseDto;
import org.example.logistics.entity.PurchaseOrder;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.exception.BadRequestException;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.PurchaseOrderCancelMapper;
import org.example.logistics.repository.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderCancelService {

    @Autowired
    private  PurchaseOrderRepository purchaseOrderRepository;
    @Autowired
    private  PurchaseOrderCancelMapper purchaseOrderCancelMapper;


    @Transactional
    public PurchaseOrderCancelResponseDto cancelPurchaseOrder(PurchaseOrderCancelDto dto) {
        log.info("Début annulation Purchase Order ID: {}", dto.getPoId());

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(dto.getPoId())
                .orElseThrow(() -> ResourceNotFoundException.withId("Purchase Order", dto.getPoId()));

        if (purchaseOrder.getStatus() != Status.APPROVED) {
            throw new BadRequestException(
                    String.format(
                            "Le Purchase Order #%d avec le statut '%s' ne peut pas être annulé. " +
                                    "Seuls les PO APPROVED (non encore réceptionnés) peuvent être annulés.",
                            dto.getPoId(), purchaseOrder.getStatus()
                    )
            );
        }

        Status previousStatus = purchaseOrder.getStatus();

        purchaseOrder.setStatus(Status.CANCELED);
        purchaseOrder.setCanceledAt(LocalDateTime.now());

        PurchaseOrder savedPo = purchaseOrderRepository.save(purchaseOrder);

        log.info("Purchase Order ID: {} annulé avec succès. Statut: {} → CANCELED",
                savedPo.getId(), previousStatus);

        PurchaseOrderCancelResponseDto response = purchaseOrderCancelMapper.toDto(savedPo);
        response.setPreviousStatus(previousStatus.name());
        response.setMessage(String.format(
                "Purchase Order #%d annulé avec succès. Aucune réception n'avait été effectuée.",
                savedPo.getId()
        ));

        return response;
    }


    @Transactional(readOnly = true)
    public List<PurchaseOrder> getCanceledPurchaseOrdersBetween(
            LocalDateTime startDate, LocalDateTime endDate) {
        return purchaseOrderRepository.findCanceledPurchaseOrdersBetween(startDate, endDate);
    }


    @Transactional(readOnly = true)
    public long countCanceledPurchaseOrders() {
        return purchaseOrderRepository.countByStatus(Status.CANCELED);
    }
}