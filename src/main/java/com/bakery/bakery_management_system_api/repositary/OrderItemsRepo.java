package com.bakery.bakery_management_system_api.repositary;

import com.bakery.bakery_management_system_api.entity.Orders.OrderDetails;
import com.bakery.bakery_management_system_api.entity.Orders.OrderedItems;
import com.bakery.bakery_management_system_api.enums.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemsRepo extends JpaRepository<OrderedItems, Long> {

    void deleteByOrderDetailsAndShift(OrderDetails orderDetails , Shift shift);
    List <OrderedItems> findByOrderDetailsAndShift(OrderDetails orderDetails , Shift shift);

}
