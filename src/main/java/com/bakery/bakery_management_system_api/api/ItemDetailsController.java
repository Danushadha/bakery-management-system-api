package com.bakery.bakery_management_system_api.api;

import com.bakery.bakery_management_system_api.dto.request.ItemDetailsRequestDTO;
import com.bakery.bakery_management_system_api.dto.response.ItemDetailsResponseDTO;
import com.bakery.bakery_management_system_api.serviceImpl.ItemDetailsService;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itemDetailsController")
@CrossOrigin(origins = "http://localhost:5173")


public class ItemDetailsController {

    private final ItemDetailsService itemDetailsService;

    public ItemDetailsController(ItemDetailsService itemDetailsService) {
        this.itemDetailsService = itemDetailsService;
    }
    @PostMapping
    public void saveItem (@RequestBody ItemDetailsRequestDTO dto){
        System.out.println(dto.getItemName());

        itemDetailsService.saveItem(dto);
    }

    @GetMapping
    public List <ItemDetailsResponseDTO> getItemList(){

        return  itemDetailsService.getAllItems();
    }
}
