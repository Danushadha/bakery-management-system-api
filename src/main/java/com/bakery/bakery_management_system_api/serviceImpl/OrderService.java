package com.bakery.bakery_management_system_api.serviceImpl;


import com.bakery.bakery_management_system_api.dto.request.OrderRequestDTO;
import com.bakery.bakery_management_system_api.dto.request.OrderedItemsRequestDto;
import com.bakery.bakery_management_system_api.dto.response.orderResponseDTOS.OrdereResponseDto;
import com.bakery.bakery_management_system_api.dto.response.orderResponseDTOS.OrderedItemsResponseDto;
import com.bakery.bakery_management_system_api.entity.ItemDetails;
import com.bakery.bakery_management_system_api.entity.Orders.OrderDetails;
import com.bakery.bakery_management_system_api.entity.Orders.OrderedItems;
import com.bakery.bakery_management_system_api.entity.Users;
import com.bakery.bakery_management_system_api.entity.VehicleDetails;
import com.bakery.bakery_management_system_api.enums.OrderStatus;
import com.bakery.bakery_management_system_api.enums.Shift;
import com.bakery.bakery_management_system_api.repositary.*;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
    private final UserRepo userRepo;

    public OrderService(OrderRepo orderRepo, ItemDetailsRepo itemDetailsRepo, OrderItemsRepo orderItemsRepo, VehicleDetailsRepo vehicleDetailsRepo, UserRepo userRepo) {
        this.orderRepo = orderRepo;
        this.itemDetailsRepo = itemDetailsRepo;
        this.orderItemsRepo = orderItemsRepo;
        this.vehicleDetailsRepo = vehicleDetailsRepo;
        this.userRepo = userRepo;
    }

    private Users getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        return userRepo.findByUserName(username)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated User Not found"));
    }

    @Transactional
    public OrdereResponseDto saveMorningOrder (OrderRequestDTO dto){

        Users currentUser = getCurrentUser();


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
            order.setUsers(currentUser);

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

            order = orderRepo
                    .findByIdAndUsers(dto.getOrderId(), currentUser)
                    .orElseThrow(() ->
                            new RuntimeException("Order not found or access denied"));

            orderItemsRepo.deleteByOrderDetailsAndShift(order , Shift.MORNING);
        }

        BigDecimal morningTotal = BigDecimal.ZERO;


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


        order.setGrandTotal(order.getMorningTotal().add(order.getEveningTotal()));
        order = orderRepo.save(order);


        //setting values back to response
        OrdereResponseDto responseDtodto = new OrdereResponseDto();
        responseDtodto.setId(order.getId());
        responseDtodto.setOrderNo(order.getOrderNo());
        responseDtodto.setOrderDate(order.getCreatedAt());
        responseDtodto.setVehicle(order.getVehicle());
        responseDtodto.setStatus(order.getStatus());
        responseDtodto.setMorningTotal(order.getMorningTotal());
        responseDtodto.setGrandtotal(order.getGrandTotal());


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

    @Transactional
    public  OrdereResponseDto saveEveningOrder (OrderRequestDTO dto){

        // here find the respective morning order of vehicle




        OrderDetails order;

        VehicleDetails vehicle = vehicleDetailsRepo.findById(dto.getVehicleId())
                .orElseThrow(()->new RuntimeException("Vehicle not Found ..."));

        Users currentUser = getCurrentUser();


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
            order.setUsers(currentUser);

            order = orderRepo.save(order);
            String orderNo = generateOrderNo(order.getId());

            order.setOrderNo(orderNo);
            order = orderRepo.save(order);

        }

        if (dto.getOrderId()!=null){

            order = orderRepo
                    .findByIdAndUsers(dto.getOrderId(), currentUser)
                    .orElseThrow(() ->
                            new RuntimeException("Order not found or access denied"));

            orderItemsRepo.deleteByOrderDetailsAndShift(order , Shift.EVENING);

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

        order.setGrandTotal(order.getMorningTotal().add(eveningTotal));
        order= orderRepo.save(order);


        // setting evening values to response

        OrdereResponseDto responseDtodto = new OrdereResponseDto();
        responseDtodto.setId(order.getId());
        responseDtodto.setOrderNo(order.getOrderNo());
        responseDtodto.setOrderDate(order.getCreatedAt());
        responseDtodto.setVehicle(order.getVehicle());
        responseDtodto.setStatus(order.getStatus());

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

        Users currentUser = getCurrentUser();

        VehicleDetails vehicle = vehicleDetailsRepo.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atStartOfDay();

        LocalDateTime end = today.plusDays(1).atStartOfDay();

        OrderDetails order = orderRepo
                .findByVehicleAndUsersAndCreatedAtBetween(
                        vehicle,
                        currentUser,
                        start,
                        end
                )
                .orElse(null);

        if (order == null) {
            return null;
        }

        OrdereResponseDto response = new OrdereResponseDto();

        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setOrderDate(order.getCreatedAt());
        response.setVehicle(order.getVehicle());
        response.setStatus(order.getStatus());
        response.setMorningTotal(order.getMorningTotal());
        response.setEveningTotal(order.getEveningTotal());
        response.setGrandtotal(order.getGrandTotal());

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

        List<OrderedItemsResponseDto> mdtoList = new ArrayList<>();

        for (OrderedItems item : morningItems) {

            OrderedItemsResponseDto dto = new OrderedItemsResponseDto();
            dto.setItemId(item.getItemDetails().getId());
            dto.setOrderId(order.getId());
            dto.setItemName(item.getItemDetails().getItemName());
            dto.setQty(item.getQty());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setLineTotal(item.getLineTotal());
            dto.setShift(item.getShift());

            mdtoList.add(dto);
        }

        response.setMorningItemsResponseDto(mdtoList);

        List<OrderedItemsResponseDto> edtoList = new ArrayList<>();

        for (OrderedItems item : eveningItems) {

            OrderedItemsResponseDto dto = new OrderedItemsResponseDto();
            dto.setItemId(item.getItemDetails().getId());
            dto.setOrderId(order.getId());
            dto.setItemName(item.getItemDetails().getItemName());
            dto.setQty(item.getQty());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setLineTotal(item.getLineTotal());
            dto.setShift(item.getShift());

            edtoList.add(dto);
        }

        response.setEveningItemsResponseDto(edtoList);

        return response;
    }





    public String generateOrderNo(Long id){

        int year = Year.now().getValue();
        String month = LocalDate.now().getMonth().
                getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();

        String generatedNo = "NB" + year + "/" + month + "-" + String.format("%04d",id);

        return generatedNo;

    }


    public OrdereResponseDto getCloseandPrintInvoice(Long orderId) {

        OrderDetails order = orderRepo.findById(orderId)
                .orElseThrow(()->new RuntimeException("order not found"));

        order.setStatus(OrderStatus.CLOSE);
        orderRepo.save(order);



        OrdereResponseDto response = new OrdereResponseDto();

        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setOrderDate(order.getCreatedAt());
        response.setVehicle(order.getVehicle());
        response.setStatus(order.getStatus());
        response.setMorningTotal(order.getMorningTotal());
        response.setEveningTotal(order.getEveningTotal());
        response.setGrandtotal(order.getGrandTotal());

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

        List<OrderedItemsResponseDto> mdtoList = new ArrayList<>();

        for (OrderedItems item : morningItems) {

            OrderedItemsResponseDto dto = new OrderedItemsResponseDto();
            dto.setItemId(item.getItemDetails().getId());
            dto.setOrderId(order.getId());
            dto.setItemName(item.getItemDetails().getItemName());
            dto.setQty(item.getQty());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setLineTotal(item.getLineTotal());
            dto.setShift(item.getShift());

            mdtoList.add(dto);
        }

        response.setMorningItemsResponseDto(mdtoList);

        List<OrderedItemsResponseDto> edtoList = new ArrayList<>();

        for (OrderedItems item : eveningItems) {

            OrderedItemsResponseDto dto = new OrderedItemsResponseDto();
            dto.setItemId(item.getItemDetails().getId());
            dto.setOrderId(order.getId());
            dto.setItemName(item.getItemDetails().getItemName());
            dto.setQty(item.getQty());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setLineTotal(item.getLineTotal());
            dto.setShift(item.getShift());

            edtoList.add(dto);
        }

        response.setEveningItemsResponseDto(edtoList);

        return response;
    }




// for view orders page
    public List<OrdereResponseDto> getOrdersByDate(
            LocalDate startDate,
            LocalDate endDate
    ) {

        LocalDateTime start = startDate.atStartOfDay();

        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<OrderDetails> orders =
                orderRepo.findByCreatedAtBetween(start, end);

        List<OrdereResponseDto> responseList = new ArrayList<>();

        for (OrderDetails order : orders) {

            OrdereResponseDto response = new OrdereResponseDto();

            response.setId(order.getId());
            response.setOrderNo(order.getOrderNo());
            response.setOrderDate(order.getCreatedAt());
            response.setVehicle(order.getVehicle());
            response.setStatus(order.getStatus());
            response.setMorningTotal(order.getMorningTotal());
            response.setEveningTotal(order.getEveningTotal());
            response.setGrandtotal(order.getGrandTotal());

            // Morning items
            List<OrderedItems> morningItems =
                    orderItemsRepo.findByOrderDetailsAndShift(
                            order,
                            Shift.MORNING
                    );

            List<OrderedItemsResponseDto> morningDtoList =
                    new ArrayList<>();

            for (OrderedItems item : morningItems) {

                OrderedItemsResponseDto dto =
                        new OrderedItemsResponseDto();

                dto.setItemId(item.getItemDetails().getId());
                dto.setOrderId(order.getId());
                dto.setItemName(item.getItemDetails().getItemName());
                dto.setQty(item.getQty());
                dto.setUnitPrice(item.getUnitPrice());
                dto.setLineTotal(item.getLineTotal());
                dto.setShift(item.getShift());

                morningDtoList.add(dto);
            }

            response.setMorningItemsResponseDto(morningDtoList);


            // Evening items
            List<OrderedItems> eveningItems =
                    orderItemsRepo.findByOrderDetailsAndShift(
                            order,
                            Shift.EVENING
                    );

            List<OrderedItemsResponseDto> eveningDtoList =
                    new ArrayList<>();

            for (OrderedItems item : eveningItems) {

                OrderedItemsResponseDto dto =
                        new OrderedItemsResponseDto();

                dto.setItemId(item.getItemDetails().getId());
                dto.setOrderId(order.getId());
                dto.setItemName(item.getItemDetails().getItemName());
                dto.setQty(item.getQty());
                dto.setUnitPrice(item.getUnitPrice());
                dto.setLineTotal(item.getLineTotal());
                dto.setShift(item.getShift());

                eveningDtoList.add(dto);
            }

            response.setEveningItemsResponseDto(eveningDtoList);

            responseList.add(response);
        }

        return responseList;
    }

    public List<OrdereResponseDto> getOrdersByDateForNormalUser(
            LocalDate startDate, LocalDate endDate) {


        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List <Long> vehicleIds = List.of(16L, 17L, 18L, 19L, 20L );

        List<OrderDetails> orders =
                orderRepo.findByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndVehicle_IdIn(
                        start,
                        end,
                        vehicleIds
                );
        List<OrdereResponseDto> responseList = new ArrayList<>();

        for (OrderDetails order : orders) {

            OrdereResponseDto response = new OrdereResponseDto();

            response.setId(order.getId());
            response.setOrderNo(order.getOrderNo());
            response.setOrderDate(order.getCreatedAt());
            response.setVehicle(order.getVehicle());
            response.setStatus(order.getStatus());
            response.setMorningTotal(order.getMorningTotal());
            response.setEveningTotal(order.getEveningTotal());
            response.setGrandtotal(order.getGrandTotal());

            // Morning items
            List<OrderedItems> morningItems =
                    orderItemsRepo.findByOrderDetailsAndShift(
                            order,
                            Shift.MORNING
                    );

            List<OrderedItemsResponseDto> morningDtoList =
                    new ArrayList<>();

            for (OrderedItems item : morningItems) {

                OrderedItemsResponseDto dto =
                        new OrderedItemsResponseDto();

                dto.setItemId(item.getItemDetails().getId());
                dto.setOrderId(order.getId());
                dto.setItemName(item.getItemDetails().getItemName());
                dto.setQty(item.getQty());
                dto.setUnitPrice(item.getUnitPrice());
                dto.setLineTotal(item.getLineTotal());
                dto.setShift(item.getShift());

                morningDtoList.add(dto);
            }

            response.setMorningItemsResponseDto(morningDtoList);


            // Evening items
            List<OrderedItems> eveningItems =
                    orderItemsRepo.findByOrderDetailsAndShift(
                            order,
                            Shift.EVENING
                    );

            List<OrderedItemsResponseDto> eveningDtoList =
                    new ArrayList<>();

            for (OrderedItems item : eveningItems) {

                OrderedItemsResponseDto dto =
                        new OrderedItemsResponseDto();

                dto.setItemId(item.getItemDetails().getId());
                dto.setOrderId(order.getId());
                dto.setItemName(item.getItemDetails().getItemName());
                dto.setQty(item.getQty());
                dto.setUnitPrice(item.getUnitPrice());
                dto.setLineTotal(item.getLineTotal());
                dto.setShift(item.getShift());

                eveningDtoList.add(dto);
            }

            response.setEveningItemsResponseDto(eveningDtoList);

            responseList.add(response);
        }

        return responseList;
    }
}
