package com.bakery.bakery_management_system_api.repositary;

import com.bakery.bakery_management_system_api.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository <Users , Long>{

    Optional<Users> findByUserName(String username);
}
