package com.bakery.bakery_management_system_api.dto.response.orderResponseDTOS;

import com.bakery.bakery_management_system_api.dto.response.VehicleDetailsResponseDTO;
import com.bakery.bakery_management_system_api.entity.VehicleDetails;
import com.bakery.bakery_management_system_api.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.converter.json.GsonBuilderUtils;

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
    private OrderStatus status;
    private BigDecimal morningTotal;
    private BigDecimal eveningTotal;
    private BigDecimal grandTotal;
    private BigDecimal vehicleRent;

    private List <OrderedItemsResponseDto> morningItemsResponseDto;
    private List <OrderedItemsResponseDto> eveningItemsResponseDto;


}
