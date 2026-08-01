package com.bakery.bakery_management_system_api.repositary;


import com.bakery.bakery_management_system_api.entity.Orders.OrderDetails;
import com.bakery.bakery_management_system_api.entity.VehicleDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepo extends JpaRepository<OrderDetails, Long> {

    Optional<OrderDetails> findByVehicleAndCreatedAtBetween(
            VehicleDetails vehicle,
            LocalDateTime start,
            LocalDateTime end
    );
}
