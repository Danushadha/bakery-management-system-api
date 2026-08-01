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
public class ItemDetails {
    @Id
    @Column(name = "item_id" )
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name" , nullable = false , length = 25)
    private String itemName;

    @Column(name = "price" , nullable = false , length = 25)
    private BigDecimal price;

    @CreationTimestamp
    private LocalDateTime createdTime;
}
