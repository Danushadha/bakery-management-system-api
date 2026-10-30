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
import com.bakery.bakery_management_system_api.enums.ItemStatus;
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

    private OrderDetails findOrderforVehicleAndDate(VehicleDetails vehicle , LocalDate orderDate , Users currentUser){

        LocalDateTime start = orderDate.atStartOfDay();
        LocalDateTime end = orderDate.plusDays(1)
                .atStartOfDay()
                .minusNanos(1);

        return orderRepo
                .findByVehicleAndUsersAndCreatedAtBetween(
                        vehicle,
                        currentUser,
                        start,
                        end
                )
                .orElse(null);
    }

    @Transactional
    public OrdereResponseDto saveMorningOrder(OrderRequestDTO dto, String ipAddress) {

        // 1. Find the selected vehicle
        VehicleDetails vehicle = vehicleDetailsRepo.findById(dto.getVehicleId())
                .orElseThrow(() ->
                        new RuntimeException("Vehicle not found"));

        // 2. Get the currently authenticated user
        Users currentUser = getCurrentUser();

        OrderDetails order;

        // 3. Check whether the frontend supplied an existing order ID
        if (dto.getOrderId() != null) {

            // Find the order belonging to the current user
            order = orderRepo.findByIdAndUsers(
                            dto.getOrderId(), currentUser)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Order not found for the current user"));

            // Make sure the order belongs to the selected vehicle and date
            if (!order.getVehicle().getId().equals(vehicle.getId())
                    || !order.getCreatedAt().toLocalDate()
                    .equals(dto.getOrderDate())) {

                throw new RuntimeException(
                        "The selected order does not match the vehicle and date");
            }

            // Do not allow changes to a closed order
            if (order.getStatus() == OrderStatus.CLOSE) {
                throw new RuntimeException(
                        "This order is already closed");
            }

            // Remove the existing morning items before replacing them
            orderItemsRepo.deleteByOrderDetailsAndShift(
                    order, Shift.MORNING);

        } else {

            // 4. No order ID supplied: search for an order for this
            // vehicle, current user, and selected date
            order = findOrderforVehicleAndDate(
                    vehicle, dto.getOrderDate(), currentUser);

            if (order != null) {

                // An existing closed order cannot be changed
                if (order.getStatus() == OrderStatus.CLOSE) {
                    throw new RuntimeException(
                            "An order already exists for this date and is closed");
                }

                // Do not create a duplicate morning shift
                boolean morningExists =
                        orderItemsRepo.existsByOrderDetailsAndShift(
                                order, Shift.MORNING);

                if (morningExists) {
                    throw new RuntimeException(
                            "A morning order already exists for this vehicle and date");
                }

                // If this order contains only evening items,
                // we can add the morning items to the same order.

            } else {

                // 5. No order exists: create a new order
                order = new OrderDetails();

                order.setVehicle(vehicle);
                order.setUsers(currentUser);

                order.setMorningTotal(BigDecimal.ZERO);
                order.setEveningTotal(BigDecimal.ZERO);
                order.setGrandTotal(BigDecimal.ZERO);
                order.setVehicleRent(BigDecimal.ZERO);

                order.setStatus(OrderStatus.OPEN);
                order.setCreatedAt(dto.getOrderDate().atStartOfDay());
                order.setIpAddress(ipAddress);

                // Save first to generate the database ID
                order = orderRepo.save(order);

                // Generate the order number using the generated ID
                String orderNo = generateOrderNo(
                        order.getId(), dto.getOrderDate());

                order.setOrderNo(orderNo);

                order = orderRepo.save(order);
            }
        }

        // 6. Save morning items for BOTH new and existing orders
        BigDecimal morningTotal = BigDecimal.ZERO;

        for (OrderedItemsRequestDto itemReqDto
                : dto.getOrderedItemsRequestDtoList()) {

            // Only allow active items
            ItemDetails item = itemDetailsRepo.findByIdAndItemStatus(
                            itemReqDto.getItemId(), ItemStatus.ACTIVE)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Item not found or inactive"));

            // Validate quantity
            if (itemReqDto.getQty() <= 0) {
                throw new RuntimeException(
                        "Item quantity must be greater than zero");
            }

            OrderedItems orderedItems = new OrderedItems();

            orderedItems.setOrderDetails(order);
            orderedItems.setItemDetails(item);
            orderedItems.setQty(itemReqDto.getQty());
            orderedItems.setUnitPrice(item.getPrice());
            orderedItems.setShift(Shift.MORNING);

            // Calculate line total
            BigDecimal lineTotal = item.getPrice()
                    .multiply(BigDecimal.valueOf(itemReqDto.getQty()));

            orderedItems.setLineTotal(lineTotal);

            morningTotal = morningTotal.add(lineTotal);

            orderItemsRepo.save(orderedItems);
        }

        // 7. Update the morning total
        order.setMorningTotal(morningTotal);

        // 8. Recalculate vehicle rent based on saved shifts
        updateVehicleRent(order);

        // 9. Calculate the grand total
        order.setGrandTotal(
                order.getMorningTotal()
                        .add(order.getEveningTotal() == null
                                ? BigDecimal.ZERO
                                : order.getEveningTotal())
                        .add(order.getVehicleRent() == null
                                ? BigDecimal.ZERO
                                : order.getVehicleRent())
        );

        // Save the updated totals
        order = orderRepo.save(order);

        // 10. Prepare the response DTO
        OrdereResponseDto responseDto = new OrdereResponseDto();

        responseDto.setId(order.getId());
        responseDto.setOrderNo(order.getOrderNo());
        responseDto.setOrderDate(order.getCreatedAt());
        responseDto.setVehicle(order.getVehicle());
        responseDto.setVehicleRent(order.getVehicleRent());
        responseDto.setStatus(order.getStatus());
        responseDto.setMorningTotal(order.getMorningTotal());
        responseDto.setGrandTotal(order.getGrandTotal());

        // 11. Retrieve the saved morning items
        List<OrderedItems> orderedItemsList =
                orderItemsRepo.findByOrderDetailsAndShift(
                        order, Shift.MORNING);

        List<OrderedItemsResponseDto> orderedItemsResponseDtos =
                new ArrayList<>();

        for (OrderedItems items : orderedItemsList) {

            OrderedItemsResponseDto itemResponse =
                    new OrderedItemsResponseDto();

            itemResponse.setItemId(items.getItemDetails().getId());
            itemResponse.setOrderId(items.getOrderDetails().getId());
            itemResponse.setItemName(items.getItemDetails().getItemName());
            itemResponse.setQty(items.getQty());
            itemResponse.setUnitPrice(items.getUnitPrice());
            itemResponse.setLineTotal(items.getLineTotal());

            orderedItemsResponseDtos.add(itemResponse);
        }

        responseDto.setMorningItemsResponseDto(
                orderedItemsResponseDtos);

        return responseDto;
    }

    @Transactional
    public OrdereResponseDto saveEveningOrder(
            OrderRequestDTO dto,
            String ipAddress) {

        // 1. Find the selected vehicle
        VehicleDetails vehicle = vehicleDetailsRepo.findById(dto.getVehicleId())
                .orElseThrow(() ->
                        new RuntimeException("Vehicle not found"));

        // 2. Get the currently authenticated user
        Users currentUser = getCurrentUser();

        OrderDetails order;

        // 3. Check whether the frontend supplied an existing order ID
        if (dto.getOrderId() != null) {

            // Find the order belonging to the current user
            order = orderRepo.findByIdAndUsers(
                            dto.getOrderId(), currentUser)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Evening order not found or access denied"));

            // Validate vehicle and date
            if (!order.getVehicle().getId().equals(vehicle.getId()) ||
                    !order.getCreatedAt().toLocalDate()
                            .equals(dto.getOrderDate())) {

                throw new RuntimeException(
                        "The order does not match the selected vehicle and date");
            }

            // Check whether the order is closed
            if (order.getStatus() == OrderStatus.CLOSE) {
                throw new RuntimeException(
                        "This order is closed and cannot be edited");
            }

            // Delete previous evening items before replacing them
            orderItemsRepo.deleteByOrderDetailsAndShift(
                    order, Shift.EVENING);

        } else {

            // 4. No order ID supplied:
            // Search for an existing order for this vehicle, user and date

            order = findOrderforVehicleAndDate(
                    vehicle, dto.getOrderDate(), currentUser);

            if (order != null) {

                // Reject closed orders
                if (order.getStatus() == OrderStatus.CLOSE) {
                    throw new RuntimeException(
                            "An order already exists for this date and is closed");
                }

                // Check whether evening items already exist
                boolean eveningExists =
                        orderItemsRepo.existsByOrderDetailsAndShift(
                                order, Shift.EVENING);

                if (eveningExists) {
                    throw new RuntimeException(
                            "An evening order already exists for this vehicle and date");
                }

                // IMPORTANT:
                // Reuse the existing order.
                // This could be a morning-only order.

            } else {

                // 5. No existing order: create a new evening-only order

                order = new OrderDetails();

                order.setVehicle(vehicle);
                order.setUsers(currentUser);

                order.setMorningTotal(BigDecimal.ZERO);
                order.setEveningTotal(BigDecimal.ZERO);
                order.setGrandTotal(BigDecimal.ZERO);
                order.setVehicleRent(BigDecimal.ZERO);

                order.setStatus(OrderStatus.OPEN);
                order.setCreatedAt(dto.getOrderDate().atStartOfDay());
                order.setIpAddress(ipAddress);

                // Save first to generate the database ID
                order = orderRepo.save(order);

                // Generate the order number
                String orderNo = generateOrderNo(
                        order.getId(), dto.getOrderDate());

                order.setOrderNo(orderNo);

                order = orderRepo.save(order);
            }
        }

        // 6. Save evening items for BOTH new and existing orders

        BigDecimal eveningTotal = BigDecimal.ZERO;

        for (OrderedItemsRequestDto itemReqDto
                : dto.getOrderedItemsRequestDtoList()) {

            // Find active item
            ItemDetails item = itemDetailsRepo.findByIdAndItemStatus(
                            itemReqDto.getItemId(), ItemStatus.ACTIVE)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Item not found or inactive"));

            // Validate quantity
            if (itemReqDto.getQty() <= 0) {
                throw new RuntimeException(
                        "Item quantity must be greater than zero");
            }

            OrderedItems orderedItems = new OrderedItems();

            orderedItems.setOrderDetails(order);
            orderedItems.setItemDetails(item);
            orderedItems.setQty(itemReqDto.getQty());
            orderedItems.setUnitPrice(item.getPrice());
            orderedItems.setShift(Shift.EVENING);

            // Calculate line total
            BigDecimal lineTotal = item.getPrice()
                    .multiply(BigDecimal.valueOf(itemReqDto.getQty()));

            orderedItems.setLineTotal(lineTotal);

            eveningTotal = eveningTotal.add(lineTotal);

            orderItemsRepo.save(orderedItems);
        }

        // 7. Update evening total
        order.setEveningTotal(eveningTotal);

        // 8. Recalculate vehicle rent
        updateVehicleRent(order);

        // 9. Calculate grand total
        order.setGrandTotal(
                (order.getMorningTotal() == null
                        ? BigDecimal.ZERO
                        : order.getMorningTotal())
                        .add(order.getEveningTotal())
                        .add(order.getVehicleRent() == null
                                ? BigDecimal.ZERO
                                : order.getVehicleRent())
        );

        // Save updated order
        order = orderRepo.save(order);

        // 10. Prepare response DTO
        OrdereResponseDto responseDto = new OrdereResponseDto();

        responseDto.setId(order.getId());
        responseDto.setOrderNo(order.getOrderNo());
        responseDto.setOrderDate(order.getCreatedAt());
        responseDto.setVehicle(order.getVehicle());
        responseDto.setVehicleRent(order.getVehicleRent());
        responseDto.setStatus(order.getStatus());
        responseDto.setGrandTotal(order.getGrandTotal());
        responseDto.setEveningTotal(order.getEveningTotal());

        // 11. Retrieve saved evening items
        List<OrderedItems> orderedItemsList =
                orderItemsRepo.findByOrderDetailsAndShift(
                        order, Shift.EVENING);

        List<OrderedItemsResponseDto> orderedItemsResponseDtos =
                new ArrayList<>();

        for (OrderedItems items : orderedItemsList) {

            OrderedItemsResponseDto itemResponse =
                    new OrderedItemsResponseDto();

            itemResponse.setItemId(items.getItemDetails().getId());
            itemResponse.setOrderId(items.getOrderDetails().getId());
            itemResponse.setItemName(items.getItemDetails().getItemName());
            itemResponse.setQty(items.getQty());
            itemResponse.setUnitPrice(items.getUnitPrice());
            itemResponse.setLineTotal(items.getLineTotal());

            orderedItemsResponseDtos.add(itemResponse);
        }

        responseDto.setEveningItemsResponseDto(
                orderedItemsResponseDtos);

        return responseDto;
    }


    private void updateVehicleRent(OrderDetails order) {

        BigDecimal fullRent = order.getVehicle().getRent();

        if (fullRent == null || fullRent.compareTo(BigDecimal.ZERO) <= 0) {
            order.setVehicleRent(BigDecimal.ZERO);
            return;
        }

        boolean hasMorning = orderItemsRepo.existsByOrderDetailsAndShift(order ,Shift.MORNING);

        boolean hasEvening = orderItemsRepo.existsByOrderDetailsAndShift(order ,Shift.EVENING);

        if (hasMorning && hasEvening ){

            order.setVehicleRent(fullRent);
        }

        else {

            order.setVehicleRent(fullRent.divide(BigDecimal.valueOf(2)));
        }

    }

// this methods call when user selects the vehicle
    public OrdereResponseDto getOrderByVehicleAndDate(Long vehicleId, LocalDate orderDate) {

        Users currentUser = getCurrentUser();

        VehicleDetails vehicle = vehicleDetailsRepo.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));



        LocalDateTime start = orderDate.atStartOfDay();

        LocalDateTime end = orderDate.plusDays(1).atStartOfDay();

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
        response.setVehicleRent(order.getVehicleRent());
        response.setStatus(order.getStatus());
        response.setMorningTotal(order.getMorningTotal());
        response.setEveningTotal(order.getEveningTotal());
        response.setVehicleRent(order.getVehicleRent());
        response.setGrandTotal(order.getGrandTotal());

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





    public String generateOrderNo(Long id , LocalDate orderDate){

        int year = Year.now().getValue();
        String month = orderDate.getMonth().
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
        response.setVehicleRent(order.getVehicleRent());
        response.setGrandTotal(order.getGrandTotal());

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
            response.setGrandTotal(order.getGrandTotal());

            // Morning items
            List<OrderedItems> morningItems =
                    orderItemsRepo.findByOrderDetailsAndShift(
                            order,
                            Shift.MORNING
                    );

            List<OrderedItemsResponseDto> morningDtoList =
                    new ArrayList<>();

            for (OrderedItems item : morningItems) {

                OrderedItemsResponseDto dto = new OrderedItemsResponseDto();

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
            response.setGrandTotal(order.getGrandTotal());

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
