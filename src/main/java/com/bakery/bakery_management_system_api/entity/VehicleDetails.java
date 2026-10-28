package com.bakery.bakery_management_system_api.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class VehicleDetails {


        @Id
        @Column(name = "vehicle_id" )
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "vehicle_number" , nullable = false , length = 25)
        private String vehicleNumber;

        @Column(name = "driver_name" , nullable = false , length = 25)
        private String driverName;

        @Column(name = "rent" , nullable = true , length = 25)
        private BigDecimal rent;

        @CreationTimestamp
        @Column (name = "createdAt" , nullable = true, length = 100, updatable = false)
        private LocalDateTime createdAt;


}
