package com.bakery.bakery_management_system_api.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderRequestDTO {

    private Long orderId;
    private Long vehicleId;
    private LocalDate orderDate;

    private List<OrderedItemsRequestDto> orderedItemsRequestDtoList;
}
