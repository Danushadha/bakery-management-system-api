package com.bakery.bakery_management_system_api.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderRequestDTO {

    private Long orderId;
    private Long vehicleId;

    private List<OrderedItemsRequestDto> orderedItemsRequestDtoList;
}
