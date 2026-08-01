package com.bakery.bakery_management_system_api.entity.Orders;

import com.bakery.bakery_management_system_api.entity.ItemDetails;
import com.bakery.bakery_management_system_api.enums.Shift;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderedItems {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "ordered_items_id")
    private Long id;

    @ManyToOne
    @JoinColumn (name = "order_id")
    private OrderDetails orderDetails;

    @ManyToOne
    @JoinColumn (name = "item_id")
    private ItemDetails itemDetails;

    @Column (name = "qty", nullable = false, length = 100)
    private Long qty;

    @Column (name = "unit_price" , nullable = false, length = 100)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false)
    private BigDecimal lineTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift" , nullable = false)
    private Shift shift;

}
