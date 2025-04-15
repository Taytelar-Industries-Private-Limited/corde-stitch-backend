package com.cordestitch.entity.alteration;

import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.entity.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "slot_data")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SlotEntity {

    @Id
    @Column(name = "slot_id")
    private String slotId;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "slot_booked", nullable = false)
    private Boolean slotBooked;

    @Column(name = "is_new_measurement", nullable = false)
    private Boolean isNewMeasurement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    @ToString.Exclude
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", referencedColumnName = "address_id", nullable = false)
    @ToString.Exclude
    private AddressEntity addressEntity;

    @OneToMany(mappedBy = "slotEntity", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<OrderItemEntity> orderItems;

}