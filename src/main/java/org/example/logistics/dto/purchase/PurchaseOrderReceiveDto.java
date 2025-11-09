package org.example.logistics.dto.purchase;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PurchaseOrderReceiveDto {
    @NotEmpty(message = "Quantités reçues requises")
    private List<LineReceiveDto> lines;

    @Data
    @Builder
    public static class LineReceiveDto {
        @Min(value = 0, message = "Quantité reçue >=0")
        private Integer receivedQuantity;
    }
}
