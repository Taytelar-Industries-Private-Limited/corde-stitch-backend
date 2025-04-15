package com.cordestitch.repository.alteration;

import com.cordestitch.entity.alteration.SlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<SlotEntity, String> {
    List<SlotEntity> findBySlotDate(LocalDate todayDate);

    Optional<SlotEntity> findByUserEntityUserIdAndSlotDateAndStartTimeAndEndTime(String userId, LocalDate slotDate, LocalTime startTime, LocalTime endTime);

    List<SlotEntity> findAllByUserEntityUserId(String userId);

    Optional<SlotEntity> findBySlotDateAndStartTimeAndEndTime(LocalDate slotDate, LocalTime startTime, LocalTime endTime);
}
