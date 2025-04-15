package com.cordestitch.repository.customization;

import com.cordestitch.entity.customization.UserCustomizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserCustomizationRepository extends JpaRepository<UserCustomizationEntity,String> {
}