package com.bakery.bakery_management_system_api.serviceImpl;

import com.bakery.bakery_management_system_api.dto.request.VehicleDetailsRequestDTO;
import com.bakery.bakery_management_system_api.dto.response.VehicleDetailsResponseDTO;
import com.bakery.bakery_management_system_api.entity.VehicleDetails;
import com.bakery.bakery_management_system_api.repositary.VehicleDetailsRepo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        vehicleDetails.setRent(dto.getRent());

        vehicleDetailsRepo.save(vehicleDetails);

    }

    public List<VehicleDetailsResponseDTO> getAllvehicleList() {

        return vehicleDetailsRepo.findAll().stream().map(vehicles->{
            VehicleDetailsResponseDTO vehicleDetailsResponseDTO = new VehicleDetailsResponseDTO();

            vehicleDetailsResponseDTO.setId(vehicles.getId());
            vehicleDetailsResponseDTO.setVehicleNumber(vehicles.getVehicleNumber());
            vehicleDetailsResponseDTO.setDriverName(vehicles.getDriverName());
            vehicleDetailsResponseDTO.setRent(vehicles.getRent());

            return vehicleDetailsResponseDTO;
        }).collect(Collectors.toList());
    }

    public List<VehicleDetailsResponseDTO> getNrmlUservehicleList() {

        List <Long> ids = List.of(16L , 17L, 18L, 19L, 20L);

        List <VehicleDetails> vehicleDetails =vehicleDetailsRepo.findAllById(ids);

      return vehicleDetails.stream().map(vehicle->{

            VehicleDetailsResponseDTO vehicleDTO = new VehicleDetailsResponseDTO();


            vehicleDTO.setId(vehicle.getId());
            vehicleDTO.setDriverName(vehicle.getDriverName());
            vehicleDTO.setVehicleNumber(vehicle.getVehicleNumber());
            vehicleDTO.setRent(vehicle.getRent());

            return vehicleDTO;

        } ).collect(Collectors.toList());
    }

    public void updateVehicle(VehicleDetailsRequestDTO dto) {


        VehicleDetails vehicle = vehicleDetailsRepo.findById(dto.getId())
                .orElseThrow(()->new RuntimeException("no Vehicle found to update for this item "));

        if (!vehicle.getId().equals(dto.getId())){
            throw new RuntimeException("Selected vehicle does not match with the Database vehicle");
        }

        else {

            vehicle.setVehicleNumber(dto.getVehicleNumber());
            vehicle.setDriverName(dto.getDriverName());
            vehicle.setRent(dto.getRent());

            VehicleDetails updatevhcl= vehicleDetailsRepo.save(vehicle);


            System.out.println("updated vehicle driver" + updatevhcl.getDriverName());

        }
    }
}
