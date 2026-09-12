package com.bakery.bakery_management_system_api.repositary;


import com.bakery.bakery_management_system_api.entity.Orders.OrderDetails;
import com.bakery.bakery_management_system_api.entity.Users;
import com.bakery.bakery_management_system_api.entity.VehicleDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepo extends JpaRepository<OrderDetails, Long> {

    Optional<OrderDetails> findByIdAndUsers(
            Long orderId,
            Users users
    );

    Optional<OrderDetails> findByVehicleAndUsersAndCreatedAtBetween(
            VehicleDetails vehicle,
            Users users,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<OrderDetails> findByVehicleAndCreatedAtBetween(
            VehicleDetails vehicle,
            LocalDateTime start,
            LocalDateTime end
    );

    List<OrderDetails> findByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    List<OrderDetails> findByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndVehicle_IdIn(
            LocalDateTime start,
            LocalDateTime end,
            List<Long> vehicleIds
    );
}
