package com.bakery.bakery_management_system_api.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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
}
