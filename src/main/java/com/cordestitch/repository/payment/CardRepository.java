package com.cordestitch.repository.payment;

import com.cordestitch.entity.payment.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, String> {
    List<CardEntity> findByUserEntityUserId(String userId);

    Optional<CardEntity> findByCardIdAndUserEntityUserId(String cardId, String userId);
}