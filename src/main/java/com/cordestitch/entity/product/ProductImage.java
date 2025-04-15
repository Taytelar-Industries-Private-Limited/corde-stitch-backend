package com.cordestitch.entity.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@ToString
public class ProductImage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "product_image_id")
    private String  productImageId;

    @Column(name = "color_name")
    private String colorName;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_priority")
    private Integer imagePriority;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    private Product product;


}
