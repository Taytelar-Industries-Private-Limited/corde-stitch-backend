package com.cordestitch.entity.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
public class SubCategory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "sub_category_id")
    private String subCategoryId;

    @Column(name = "sub_category_name")
    private String subCategoryName;

    @Column(name = "sub_category_description")
    private String subCategoryDescription;

    @ManyToOne
    @JoinColumn(name = "category_id",referencedColumnName = "category_id")
    private Category category;

    @OneToMany(mappedBy ="subCategory",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;
}
