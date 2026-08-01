package com.bakery.bakery_management_system_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VehicleDetailsResponseDTO {
    private Long id;
    private String vehicleNumber;
    private String driverName;
}
