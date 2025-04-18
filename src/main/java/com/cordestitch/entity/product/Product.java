package com.cordestitch.entity.product;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "product_data")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Product implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "product_id")
    private String productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_status")
    private String productStatus;

    @Column(name = "product_description")
    private String productDescription;

    @Column(name = "product_material_type")
    private String productMaterialType;

    @Column(name = "product_pattern")
    private String productPattern;

    @Column(name = "product_stretch_type")
    private String productStretchType;

    @Column(name = "product_offer_percentage")
    private Double productOfferPercentage;

    @Column(name = "video_url")
    private String videoUrl;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductImage> productImages;

    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StockQuantity> stockQuantities;

    @ManyToOne
    @JoinColumn(name = "sub_category_id", referencedColumnName ="sub_category_id")
    @ToString.Exclude
    private SubCategory subCategory;
}