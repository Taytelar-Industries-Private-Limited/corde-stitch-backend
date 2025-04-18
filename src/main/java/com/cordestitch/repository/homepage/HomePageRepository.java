package com.cordestitch.repository.homepage;

import com.cordestitch.entity.homepage.HomePageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HomePageRepository extends JpaRepository<HomePageEntity, String> {
    Optional<HomePageEntity> findByDescription(String description);
}
