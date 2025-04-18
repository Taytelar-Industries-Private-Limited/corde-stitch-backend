package com.cordestitch.repository.product;

import com.cordestitch.entity.product.Product;
import com.cordestitch.entity.product.SubCategory;
import com.cordestitch.response.product.FilterTypesResponse;
import com.cordestitch.response.product.ProductFilterResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    /** Use findBySubCategory to eagerly fetch associated entities like "imageUrls" and "stockQuantities"
    when querying by subCategory. Avoid using it if you want to prevent eager loading.**/

    List<Product> findBySubCategory(SubCategory subCategoryId);

    Optional<Product> findByProductId(String productId);

    @Query("SELECT new com.cordestitch.response.product.ProductFilterResponse(c, sc, p, sq, cq) FROM Category c " +
            "JOIN c.subCategories sc " +
            "JOIN sc.products p " +
            "JOIN p.stockQuantities sq " +
            "JOIN sq.colorQuantities cq " +
            "WHERE (:productStretchType IS NULL OR p.productStretchType IN :productStretchType) " +
            "AND (:productCategory IS NULL OR sc.subCategoryName IN :productCategory) " +
            "AND (:productMaterialType IS NULL OR p.productMaterialType IN :productMaterialType) " +
            "AND (:size IS NULL OR sq.size IN :size) " +
            "AND (:colorCode IS NULL OR cq.colorCode IN :colorCode) " +
            "AND cq.quantity > 0 " +
            "ORDER BY sq.size ASC")
    List<ProductFilterResponse> findProductsByFilters(
            @Param("productStretchType") List<String> productStretchType,
            @Param("productCategory") List<String> productCategory,
            @Param("productMaterialType") List<String> productMaterialType,
            @Param("size") List<Integer> size,
            @Param("colorCode") List<String> colorCode
    );

    @Query("SELECT DISTINCT new com.cordestitch.response.product.FilterTypesResponse(" +
            "p.productStretchType, p.productMaterialType, sc.subCategoryName, sq.size, cq.color, cq.colorCode) " +
            "FROM Category c " +
            "JOIN c.subCategories sc " +
            "JOIN sc.products p " +
            "JOIN p.stockQuantities sq " +
            "JOIN sq.colorQuantities cq")
    List<FilterTypesResponse> fetchProductFilterTypes();


    Optional<List<Product>> findByProductStatus(String trending);

}
