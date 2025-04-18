package com.cordestitch.entity.product;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "stock_data")
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StockQuantity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "stock_id")
    private String stockId;

    @Column(name = "size")
    private Integer size;

    @Column(name = "product_price")
    private Double productPrice;

    @OneToMany(mappedBy = "stockQuantity",cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ColorQuantity> colorQuantities;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName="product_id")
    @ToString.Exclude
    private Product product;
}
