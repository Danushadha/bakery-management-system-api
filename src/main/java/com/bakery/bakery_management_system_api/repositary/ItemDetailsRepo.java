package com.bakery.bakery_management_system_api.repositary;

import com.bakery.bakery_management_system_api.entity.ItemDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemDetailsRepo extends JpaRepository <ItemDetails , Long> {
}
