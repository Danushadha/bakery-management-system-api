package com.bakery.bakery_management_system_api.serviceImpl;


import com.bakery.bakery_management_system_api.dto.request.OrderRequestDTO;
import com.bakery.bakery_management_system_api.dto.request.OrderedItemsRequestDto;
import com.bakery.bakery_management_system_api.dto.response.orderResponseDTOS.OrdereResponseDto;
import com.bakery.bakery_management_system_api.dto.response.orderResponseDTOS.OrderedItemsResponseDto;
import com.bakery.bakery_management_system_api.entity.ItemDetails;
import com.bakery.bakery_management_system_api.entity.Orders.OrderDetails;
import com.bakery.bakery_management_system_api.entity.Orders.OrderedItems;
import com.bakery.bakery_management_system_api.entity.VehicleDetails;
import com.bakery.bakery_management_system_api.enums.OrderStatus;
import com.bakery.bakery_management_system_api.enums.Shift;
import com.bakery.bakery_management_system_api.repositary.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.w3c.dom.stylesheets.LinkStyle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class OrderService {

    private final OrderRepo orderRepo;
    private final ItemDetailsRepo itemDetailsRepo;
    private final OrderItemsRepo orderItemsRepo;
    private final VehicleDetailsRepo vehicleDetailsRepo;

    public OrderService(OrderRepo orderRepo, ItemDetailsRepo itemDetailsRepo, OrderItemsRepo orderItemsRepo, VehicleDetailsRepo vehicleDetailsRepo) {
        this.orderRepo = orderRepo;
        this.itemDetailsRepo = itemDetailsRepo;
        this.orderItemsRepo = orderItemsRepo;
        this.vehicleDetailsRepo = vehicleDetailsRepo;
    }

    @Transactional
    public OrdereResponseDto saveMorningOrder (OrderRequestDTO dto){

        OrderDetails  order;

        VehicleDetails vehicle = vehicleDetailsRepo.findById(dto.getVehicleId())
                .orElseThrow(()->new RuntimeException("Vehicle not Found ..."));

        if (dto.getOrderId() == null){

            order =new OrderDetails();

            order.setVehicle(vehicle);
            order.setMorningTotal(BigDecimal.ZERO);
            order.setEveningTotal(BigDecimal.ZERO);
            order.setGrandTotal(BigDecimal.ZERO);
            order.setStatus(OrderStatus.OPEN);

            order = orderRepo.save(order);
            String orderNo = generateOrderNo(order.getId());

            order.setOrderNo(orderNo);
            order =  orderRepo.save(order);
        }
        else {

            // this piece runs when there is order id
            // (when user edits and save for second time)
            // which deletes whole items for any no of edits
            // and re-create whole items which sends back from response dto

            order = orderRepo.findById(dto.getOrderId())
                    .orElseThrow(()->new RuntimeException("order not found"));

            orderItemsRepo.deleteByOrderDetailsAndShift(order , Shift.MORNING);
        }

        BigDecimal morningTotal = BigDecimal.ZERO;
        BigDecimal eveningTotal = BigDecimal.ZERO;

       //setting values to items

        for (OrderedItemsRequestDto itemReqDto :dto.getOrderedItemsRequestDtoList()){

            ItemDetails item = itemDetailsRepo.findById(itemReqDto.getItemId())
                    .orElseThrow(()->new RuntimeException("item Not found"));

            OrderedItems orderedItems = new OrderedItems();
            orderedItems.setOrderDetails(order);
            orderedItems.setItemDetails(item);
            orderedItems.setQty(itemReqDto.getQty());
            orderedItems.setUnitPrice(item.getPrice());
            orderedItems.setShift(Shift.MORNING);

            BigDecimal lineTotal = item.getPrice()
                    .multiply(BigDecimal.valueOf(itemReqDto.getQty()));

            orderedItems.setLineTotal(lineTotal);

            morningTotal=morningTotal.add(lineTotal);


            orderItemsRepo.save(orderedItems);
        }
        order.setMorningTotal(morningTotal);

        // if this vehicle not arrived evening grand total will be morning total
        //setting evenng total to zero because its require not be null
        order.setEveningTotal(eveningTotal);

        order.setGrandTotal(order.getMorningTotal().add(eveningTotal));
        order = orderRepo.save(order);


        //setting values back to response
        OrdereResponseDto responseDtodto = new OrdereResponseDto();
        responseDtodto.setId(order.getId());
        responseDtodto.setOrderNo(order.getOrderNo());
        responseDtodto.setOrderDate(order.getCreatedAt());
        responseDtodto.setVehicle(order.getVehicle());

       List<OrderedItems> orderedItemsList = orderItemsRepo.findByOrderDetailsAndShift(order, Shift.MORNING);

       List <OrderedItemsResponseDto> orderedItemsResponseDtos = new ArrayList<>();

        for (OrderedItems items : orderedItemsList){

            OrderedItemsResponseDto responseDto = new OrderedItemsResponseDto();
            responseDto.setItemId(items.getItemDetails().getId());
            responseDto.setOrderId(items.getOrderDetails().getId());
            responseDto.setItemName(items.getItemDetails().getItemName());
            responseDto.setQty(items.getQty());
            responseDto.setUnitPrice(items.getUnitPrice());
            responseDto.setLineTotal(items.getLineTotal());

            orderedItemsResponseDtos.add(responseDto);
        }
        responseDtodto.setMorningItemsResponseDto(orderedItemsResponseDtos);

        return responseDtodto;

    }

    public  OrdereResponseDto saveEveningOrder (OrderRequestDTO dto){

        // here find the respective morning order of vehicle

        OrderDetails order;

        VehicleDetails vehicle = vehicleDetailsRepo.findById(dto.getVehicleId())
                .orElseThrow(()->new RuntimeException("Vehicle not Found ..."));

        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atStartOfDay();

        LocalDateTime end = today.atTime(23, 59, 59);

        order = orderRepo
                .findByVehicleAndCreatedAtBetween(vehicle, start, end)
                .orElse(null);

        // if no morning order of this vehicle it creates for evening
        if (order==null){

            order =new OrderDetails();

            order.setVehicle(vehicle);
            order.setEveningTotal(BigDecimal.ZERO);
            order.setMorningTotal(BigDecimal.ZERO);
            order.setGrandTotal(BigDecimal.ZERO);
            order.setStatus(OrderStatus.OPEN);

            order = orderRepo.save(order);
            String orderNo = generateOrderNo(order.getId());

            order.setOrderNo(orderNo);
            order = orderRepo.save(order);

        }

        BigDecimal eveningTotal = BigDecimal.ZERO;

        //setting values to items

        for (OrderedItemsRequestDto itemReqDto :dto.getOrderedItemsRequestDtoList()){

            ItemDetails item = itemDetailsRepo.findById(itemReqDto.getItemId())
                    .orElseThrow(()->new RuntimeException("item Not found"));

            OrderedItems orderedItems = new OrderedItems();
            orderedItems.setOrderDetails(order);
            orderedItems.setItemDetails(item);
            orderedItems.setQty(itemReqDto.getQty());
            orderedItems.setUnitPrice(item.getPrice());
            orderedItems.setShift(Shift.EVENING);

            BigDecimal lineTotal = item.getPrice()
                    .multiply(BigDecimal.valueOf(itemReqDto.getQty()));

            orderedItems.setLineTotal(lineTotal);

            eveningTotal = eveningTotal.add(lineTotal);


            orderItemsRepo.save(orderedItems);
        }
        order.setEveningTotal(eveningTotal);

        order.setGrandTotal(order.getGrandTotal().add(eveningTotal));
        order= orderRepo.save(order);


        // setting evening values to response

        OrdereResponseDto responseDtodto = new OrdereResponseDto();
        responseDtodto.setId(order.getId());
        responseDtodto.setOrderNo(order.getOrderNo());
        responseDtodto.setOrderDate(order.getCreatedAt());
        responseDtodto.setVehicle(order.getVehicle());

        List<OrderedItems> orderedItemsList = orderItemsRepo.findByOrderDetailsAndShift(order, Shift.EVENING);

        List <OrderedItemsResponseDto> orderedItemsResponseDtos = new ArrayList<>();

        for (OrderedItems items : orderedItemsList){

            OrderedItemsResponseDto responseDto = new OrderedItemsResponseDto();
            responseDto.setItemId(items.getItemDetails().getId());
            responseDto.setOrderId(items.getOrderDetails().getId());
            responseDto.setItemName(items.getItemDetails().getItemName());
            responseDto.setQty(items.getQty());
            responseDto.setUnitPrice(items.getUnitPrice());
            responseDto.setLineTotal(items.getLineTotal());

            orderedItemsResponseDtos.add(responseDto);
        }
        responseDtodto.setEveningItemsResponseDto(orderedItemsResponseDtos);

        return responseDtodto;


    }

// this methods call when user selects the vehicle
    public OrdereResponseDto getTodayOrder(Long vehicleId) {

        VehicleDetails vehicle = vehicleDetailsRepo.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atStartOfDay();

        LocalDateTime end = today.atTime(23, 59, 59);

        OrderDetails order = orderRepo
                .findByVehicleAndCreatedAtBetween(vehicle, start, end)
                .orElse(null);

        if (order == null) {
            return null;
        }

        OrdereResponseDto response = new OrdereResponseDto();

        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setOrderDate(order.getCreatedAt());
        response.setVehicle(order.getVehicle());

        List<OrderedItems> morningItems =
                orderItemsRepo.findByOrderDetailsAndShift(
                        order,
                        Shift.MORNING
                );

        List<OrderedItems> eveningItems =
                orderItemsRepo.findByOrderDetailsAndShift(
                        order,
                        Shift.EVENING
                );

        List<OrderedItemsResponseDto> dtoList = new ArrayList<>();

        for (OrderedItems item : morningItems) {

            OrderedItemsResponseDto dto = new OrderedItemsResponseDto();
            dto.setItemId(item.getItemDetails().getId());
            dto.setOrderId(order.getId());
            dto.setItemName(item.getItemDetails().getItemName());
            dto.setQty(item.getQty());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setLineTotal(item.getLineTotal());
            dto.setShift(item.getShift());

            dtoList.add(dto);
        }

        response.setMorningItemsResponseDto(dtoList);

        return response;
    }





    public String generateOrderNo(Long id){

        int year = Year.now().getValue();
        String month = LocalDate.now().getMonth().
                getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();

        String generatedNo = "NB" + year + "/" + month + "-" + String.format("%04d",id);

        return generatedNo;

    }


}
