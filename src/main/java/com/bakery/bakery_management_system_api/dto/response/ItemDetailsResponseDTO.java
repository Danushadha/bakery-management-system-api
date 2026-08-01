package com.bakery.bakery_management_system_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDetailsResponseDTO {
    private Long id;
    private String itemName;
    private BigDecimal price;
}
