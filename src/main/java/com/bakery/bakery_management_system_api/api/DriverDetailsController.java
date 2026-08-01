package com.bakery.bakery_management_system_api.api;


import com.bakery.bakery_management_system_api.dto.request.DriverRequestDTO;
import com.bakery.bakery_management_system_api.dto.response.DriverDetailsResponseDTO;
import com.bakery.bakery_management_system_api.dto.response.ItemDetailsResponseDTO;
import com.bakery.bakery_management_system_api.serviceImpl.DriverDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/DriverDetailsController")
@CrossOrigin(origins = "http://localhost:5173")
public class DriverDetailsController {

    private final DriverDetailsService driverDetailsService;

    public DriverDetailsController(DriverDetailsService driverDetailsService) {
        this.driverDetailsService = driverDetailsService;
    }

    @PostMapping
    public void saveDriverDetails (@RequestBody DriverRequestDTO dto){

            driverDetailsService.saveDriver(dto);
    }

    @GetMapping
    public List <DriverDetailsResponseDTO> getDriverList(){

        return driverDetailsService.getAllDrivers();
    }


}
