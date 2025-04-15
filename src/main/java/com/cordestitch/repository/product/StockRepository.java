package com.cordestitch.repository.product;

import com.cordestitch.entity.product.StockQuantity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<StockQuantity, String> {

}
