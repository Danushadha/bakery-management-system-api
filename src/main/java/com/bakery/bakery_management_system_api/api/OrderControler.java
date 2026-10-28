package com.bakery.bakery_management_system_api.api;


import ch.qos.logback.core.net.SyslogOutputStream;

import com.bakery.bakery_management_system_api.dto.request.OrderRequestDTO;
import com.bakery.bakery_management_system_api.dto.response.orderResponseDTOS.OrdereResponseDto;
import com.bakery.bakery_management_system_api.serviceImpl.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orderControler")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderControler {

    private final OrderService orderService;

    public OrderControler(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/morning")
    public OrdereResponseDto saveOrderMorning(@RequestBody OrderRequestDTO dto , HttpServletRequest request){

        String ipAddress = request.getHeader("X-Real-IP");

        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = request.getRemoteAddr();
        }
        
        System.out.println("ip addres is"+ipAddress);

       return orderService.saveMorningOrder(dto , ipAddress);
    }
    @PostMapping("/evening")
    public OrdereResponseDto saveOrderEvening(@RequestBody OrderRequestDTO dto,  HttpServletRequest request){

        String ipAddress = request.getHeader("X-Real-IP");

        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = request.getRemoteAddr();
        }
        return orderService.saveEveningOrder(dto , ipAddress);
    }


    @GetMapping("/getOrderByVehicleAndDate")
    public ResponseEntity<OrdereResponseDto> getOrderByVehicleAndDate(
            @RequestParam Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate orderDate)

    {
        System.out.println("vehicleId" + vehicleId );
        System.out.println("order date " + orderDate );
        OrdereResponseDto response = orderService.getOrderByVehicleAndDate(vehicleId, orderDate);

        return ResponseEntity.ok(response);

    }

    @GetMapping("/closeOrder/{orderId}")
    public ResponseEntity <OrdereResponseDto> closeOrder(@PathVariable  Long orderId){
        System.out.println("order Id is "+orderId);

        OrdereResponseDto responseDto = orderService.getCloseandPrintInvoice(orderId);

        return ResponseEntity.ok(responseDto);

    }


    @GetMapping("/getOrders")
    public List<OrdereResponseDto> getOrdersByDate(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authorities ->
                        authorities.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {

            return orderService.getOrdersByDate(
                    startDate,
                    endDate
            );

        }

        return orderService.getOrdersByDateForNormalUser(
                startDate,
                endDate
        );
    }


}
