package com.bakery.bakery_management_system_api.repositary;

import com.bakery.bakery_management_system_api.entity.ItemDetails;
import com.bakery.bakery_management_system_api.enums.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemDetailsRepo extends JpaRepository <ItemDetails , Long> {

    List<ItemDetails> findByItemStatus(ItemStatus itemStatus);

    Optional<ItemDetails> findByIdAndItemStatus(
            Long id,
            ItemStatus itemStatus
    );
}
