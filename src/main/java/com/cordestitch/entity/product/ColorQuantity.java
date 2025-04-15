package com.cordestitch.entity.product;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "color_quantity_data")
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ColorQuantity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "color_quantity_id")
    private String colorQuantityId;

    @Column(name = "color")
    private String color;

    @Column(name = "color_code")
    private String colorCode;

    @Column(name = "quantity")
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", referencedColumnName = "stock_id")
    @ToString.Exclude
    private StockQuantity stockQuantity;

}
