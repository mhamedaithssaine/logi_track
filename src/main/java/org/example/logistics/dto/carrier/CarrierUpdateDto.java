package org.example.logistics.dto.carrier;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarrierUpdateDto {

    @NotBlank(message = "Nom requis")
    private String name;

    private boolean active = true;
}
