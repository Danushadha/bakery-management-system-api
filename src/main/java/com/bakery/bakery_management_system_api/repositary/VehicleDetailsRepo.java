package com.bakery.bakery_management_system_api.repositary;

import com.bakery.bakery_management_system_api.entity.VehicleDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleDetailsRepo extends JpaRepository <VehicleDetails, Long> {
}
