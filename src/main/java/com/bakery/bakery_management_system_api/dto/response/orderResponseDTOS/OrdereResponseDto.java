package com.bakery.bakery_management_system_api.dto.response.orderResponseDTOS;

import com.bakery.bakery_management_system_api.dto.response.VehicleDetailsResponseDTO;
import com.bakery.bakery_management_system_api.entity.VehicleDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrdereResponseDto {

    private Long id;
    private String orderNo;
    private LocalDateTime orderDate;
    private VehicleDetails vehicle;

    private List <OrderedItemsResponseDto> morningItemsResponseDto;
    private List <OrderedItemsResponseDto> eveningItemsResponseDto;

}
