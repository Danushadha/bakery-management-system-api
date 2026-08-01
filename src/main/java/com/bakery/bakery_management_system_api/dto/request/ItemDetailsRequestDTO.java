package com.bakery.bakery_management_system_api.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ItemDetailsRequestDTO {
    private String itemName;
    private BigDecimal price;
}
