package org.example.logistics.service.shipment;

import org.example.logistics.dto.shipment.ShipmentCreateDto;
import org.example.logistics.dto.shipment.ShipmentResponseDto;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.Shipment;
import org.example.logistics.mapper.shipment.ShipmentMapper;
import org.example.logistics.repository.SalesOrderRepository;
import org.example.logistics.repository.ShipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ShipmentService {
    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private ShipmentMapper shipmentMapper;

    @Value("${logistics.cutoff-hour:15}")
    private int cutoffHour;

    public ShipmentResponseDto createShipment(ShipmentCreateDto dto) {
        Optional<SalesOrder> optOrder = salesOrderRepository.findByIdAndStatus(dto.getOrderId(), Status.RESERVED);
        if (optOrder.isEmpty()) {
            throw new RuntimeException("Commande non trouvée ou non réservée");
        }

        SalesOrder order = optOrder.get();

        Shipment shipment = shipmentMapper.toEntity(dto);
        shipment.setSalesOrder(order);

        // Planification cut-off
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime planned = now.withHour(cutoffHour).withMinute(0).withSecond(0);
        if (now.isAfter(planned)) {
            planned = planned.plusDays(1);
        }
        shipment.setPlannedDeparture(planned);

        Shipment saved = shipmentRepository.save(shipment);

        // Mapper réponse
        ShipmentResponseDto response = shipmentMapper.toDto(saved);
        response.setOrderId(order.getId());
        response.setMessage("Expédition créée et planifiée pour " + planned);

        return response;
    }

}
