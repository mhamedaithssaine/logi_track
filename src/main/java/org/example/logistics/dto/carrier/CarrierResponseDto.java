package org.example.logistics.dto.carrier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarrierResponseDto {
    private Long id;
    private String code;
    private String name;
    private boolean active;
}
