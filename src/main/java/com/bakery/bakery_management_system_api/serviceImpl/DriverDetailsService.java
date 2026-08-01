package com.bakery.bakery_management_system_api.serviceImpl;

import com.bakery.bakery_management_system_api.dto.request.DriverRequestDTO;
import com.bakery.bakery_management_system_api.dto.response.DriverDetailsResponseDTO;
import com.bakery.bakery_management_system_api.entity.DriverDetails;
import com.bakery.bakery_management_system_api.repositary.DriverDetailsRepo;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriverDetailsService {

    private final DriverDetailsRepo driverDetailsRepo;

    public DriverDetailsService(DriverDetailsRepo driverDetailsRepo) {
        this.driverDetailsRepo = driverDetailsRepo;
    }

    public void saveDriver (DriverRequestDTO dto){

        DriverDetails driverDetails = new DriverDetails();

        driverDetails.setNic(dto.getNic());
        driverDetails.setName(dto.getName());
        driverDetails.setContactNo(dto.getContactNo());

        driverDetailsRepo.save(driverDetails);

    }

    public List <DriverDetailsResponseDTO> getAllDrivers() {

        return driverDetailsRepo.findAll().stream().map(drivers->{
            DriverDetailsResponseDTO driverDetailsResponseDTO = new DriverDetailsResponseDTO();

            driverDetailsResponseDTO.setId(drivers.getId());
            driverDetailsResponseDTO.setNic(drivers.getNic());
            driverDetailsResponseDTO.setName(drivers.getName());
            driverDetailsResponseDTO.setContactNo(drivers.getContactNo());

            return driverDetailsResponseDTO;

        }).collect(Collectors.toList());

    }
}
