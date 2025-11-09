package org.example.logistics.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderCancelResponseDto {

    private Long id;
    private String previousStatus;
    private String currentStatus;
    private LocalDateTime canceledAt;
    private String message;
    private Integer totalLines;
    private String supplierName;
}