package com.bakery.bakery_management_system_api.dto.response.orderResponseDTOS;

import com.bakery.bakery_management_system_api.enums.Shift;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderedItemsResponseDto {

    private Long itemId;
    private Long orderId;
    private String itemName;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private Long qty;
    private Shift shift;

}
