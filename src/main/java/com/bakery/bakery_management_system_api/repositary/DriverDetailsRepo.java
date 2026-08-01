package com.bakery.bakery_management_system_api.repositary;

import com.bakery.bakery_management_system_api.entity.DriverDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverDetailsRepo extends JpaRepository<DriverDetails , Long> {
}
