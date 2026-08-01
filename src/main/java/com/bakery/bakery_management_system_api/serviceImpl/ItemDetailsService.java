package com.bakery.bakery_management_system_api.serviceImpl;

import com.bakery.bakery_management_system_api.dto.request.ItemDetailsRequestDTO;
import com.bakery.bakery_management_system_api.dto.response.ItemDetailsResponseDTO;
import com.bakery.bakery_management_system_api.entity.ItemDetails;
import com.bakery.bakery_management_system_api.repositary.ItemDetailsRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemDetailsService {
    private final ItemDetailsRepo itemDetailsRepo;

    public ItemDetailsService(ItemDetailsRepo itemDetailsRepo) {
        this.itemDetailsRepo = itemDetailsRepo;
    }

    public void saveItem (ItemDetailsRequestDTO dto){

        ItemDetails itemDetails = new ItemDetails();
        itemDetails.setItemName(dto.getItemName());
        itemDetails.setPrice(dto.getPrice());

        itemDetailsRepo.save(itemDetails);

    }

    public List <ItemDetailsResponseDTO> getAllItems(){

        return itemDetailsRepo.findAll().stream().map(item->{

            ItemDetailsResponseDTO itemDetailsResponseDTO = new ItemDetailsResponseDTO();
            itemDetailsResponseDTO.setId(item.getId());
            itemDetailsResponseDTO.setItemName(item.getItemName());
            itemDetailsResponseDTO.setPrice(item.getPrice());

            return itemDetailsResponseDTO;
        }).collect(Collectors.toList());
    }


}
