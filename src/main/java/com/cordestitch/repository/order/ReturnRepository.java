package com.cordestitch.repository.order;

import com.cordestitch.entity.order.ReturnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnRepository extends JpaRepository<ReturnEntity,Long> {
}
