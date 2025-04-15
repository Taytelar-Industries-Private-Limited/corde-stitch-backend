package com.cordestitch.repository.customization;

import com.cordestitch.entity.customization.CustomizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomizationRepository extends JpaRepository<CustomizationEntity, String> {

    Optional<CustomizationEntity> findByPantType(String pantType);

    Optional<CustomizationEntity> findByCustomizationId(String customizationId);
}
