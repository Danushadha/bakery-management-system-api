    package com.bakery.bakery_management_system_api.entity.Orders;


    import com.bakery.bakery_management_system_api.entity.Users;
    import com.bakery.bakery_management_system_api.entity.VehicleDetails;
    import com.bakery.bakery_management_system_api.enums.OrderStatus;
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
    @Table(name = "order_details")
    @AllArgsConstructor
    @NoArgsConstructor
    public class OrderDetails {

        @Id
        @GeneratedValue (strategy = GenerationType.IDENTITY)
        @Column(name = "order_id")
        private Long id;

        @Column (name = "order_no" , nullable = true, length = 100)
        private String orderNo;

        @ManyToOne
        @JoinColumn(name = "vehicle_id")
        private VehicleDetails vehicle;

        @ManyToOne
        @JoinColumn(name = "id")
        private Users users;

        @Column(name = "morning_total", nullable = true)
        private BigDecimal morningTotal;

        @Column(name = "evening_total", nullable = true)
        private BigDecimal eveningTotal;

        @Column(name = "grand_total", nullable = false)
        private BigDecimal grandTotal;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false)
        private OrderStatus status;

        @CreationTimestamp
        @Column (name = "createdAt" , updatable = false, length = 100)
        private LocalDateTime createdAt;


    }
