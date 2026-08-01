package com.bakery.bakery_management_system_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class DriverDetails {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "driver_id")
    private Long id;

    @Column(name = "nic" , nullable = true , length = 50)
    private String nic;

    @Column(name = "driver_name", nullable = false, length = 255)
    private String name;

    @Column (name = "contact_no" , nullable = false, length = 50)
    private String contactNo;
}
