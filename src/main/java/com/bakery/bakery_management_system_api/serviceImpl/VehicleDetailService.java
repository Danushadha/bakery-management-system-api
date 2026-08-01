package com.bakery.bakery_management_system_api.serviceImpl;

import com.bakery.bakery_management_system_api.dto.request.VehicleDetailsRequestDTO;
import com.bakery.bakery_management_system_api.dto.response.VehicleDetailsResponseDTO;
import com.bakery.bakery_management_system_api.entity.VehicleDetails;
import com.bakery.bakery_management_system_api.repositary.VehicleDetailsRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleDetailService {

    private final VehicleDetailsRepo vehicleDetailsRepo;

    public VehicleDetailService(VehicleDetailsRepo vehicleDetailsRepo) {
        this.vehicleDetailsRepo = vehicleDetailsRepo;
    }

    public void saveVehicle (VehicleDetailsRequestDTO dto){

        VehicleDetails vehicleDetails = new VehicleDetails();
        vehicleDetails.setVehicleNumber(dto.getVehicleNumber());
        vehicleDetails.setDriverName(dto.getDriverName());

        vehicleDetailsRepo.save(vehicleDetails);

    }

    public List<VehicleDetailsResponseDTO> getAllvehicleList() {

        return vehicleDetailsRepo.findAll().stream().map(vehicles->{
            VehicleDetailsResponseDTO vehicleDetailsResponseDTO = new VehicleDetailsResponseDTO();

            vehicleDetailsResponseDTO.setId(vehicles.getId());
            vehicleDetailsResponseDTO.setVehicleNumber(vehicles.getVehicleNumber());
            vehicleDetailsResponseDTO.setDriverName(vehicles.getDriverName());


            return vehicleDetailsResponseDTO;
        }).collect(Collectors.toList());
    }
}
