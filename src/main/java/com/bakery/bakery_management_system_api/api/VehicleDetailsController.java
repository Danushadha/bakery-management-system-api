package com.bakery.bakery_management_system_api.api;

import com.bakery.bakery_management_system_api.dto.request.VehicleDetailsRequestDTO;
import com.bakery.bakery_management_system_api.dto.response.VehicleDetailsResponseDTO;
import com.bakery.bakery_management_system_api.serviceImpl.VehicleDetailService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/vehicleDetailsController")
@CrossOrigin(origins = "http://localhost:5173")

public class VehicleDetailsController {

    private final VehicleDetailService vehicleDetailService;

    public VehicleDetailsController(VehicleDetailService vehicleDetailService) {
        this.vehicleDetailService = vehicleDetailService;
    }

    @PostMapping
    public void saveVehicle (@RequestBody VehicleDetailsRequestDTO dto){

        vehicleDetailService.saveVehicle(dto);


    }

    @GetMapping
    public List<VehicleDetailsResponseDTO> getAllVehicles(){
        // looking for the current thread of logged user

         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            boolean isAdmin=authentication.getAuthorities().stream()
                    .anyMatch(authorities-> authorities.getAuthority()
                            .equals("ROLE_ADMIN"));

            if (isAdmin){
                return vehicleDetailService.getAllvehicleList ();
            }

            return vehicleDetailService.getNrmlUservehicleList ();

    }
}
