package com.bakery.bakery_management_system_api.api;


import com.bakery.bakery_management_system_api.dto.request.OrderRequestDTO;
import com.bakery.bakery_management_system_api.dto.response.orderResponseDTOS.OrdereResponseDto;
import com.bakery.bakery_management_system_api.serviceImpl.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orderControler")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderControler {

    private final OrderService orderService;

    public OrderControler(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/morning")
    public OrdereResponseDto saveOrderMorning(@RequestBody OrderRequestDTO dto){

       return orderService.saveMorningOrder(dto);
    }
    @PostMapping("/evening")
    public OrdereResponseDto saveOrderEvening(@RequestBody OrderRequestDTO dto){

        return orderService.saveEveningOrder(dto);
    }


    @GetMapping("/today/{vehicleId}")
    public ResponseEntity<OrdereResponseDto> getTodayOrder(
            @PathVariable Long vehicleId
    ) {

        OrdereResponseDto response = orderService.getTodayOrder(vehicleId);

        return ResponseEntity.ok(response);

    }

    
}
