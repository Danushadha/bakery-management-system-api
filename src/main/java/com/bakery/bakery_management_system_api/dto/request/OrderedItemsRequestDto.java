package com.bakery.bakery_management_system_api.dto.request;


import com.bakery.bakery_management_system_api.enums.Shift;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderedItemsRequestDto {


    private Long itemId;
    private Long qty;



}
