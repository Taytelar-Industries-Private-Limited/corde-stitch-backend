package com.cordestitch.entity.user;

import com.cordestitch.entity.alteration.SlotEntity;
import com.cordestitch.entity.order.OrderEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Entity
@Table(name = "address_data")
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AddressEntity {

    @Id
    @Column(name = "address_id")
    private String addressId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phoneNumber", nullable = false)
    private String phoneNumber;

    @Column(name = "building_name")
    private String buildingName;

    @Column(name = "street_name")
    private String streetName;

    @Column(name = "city_name",nullable = false)
    private String cityName;

    @Column(name = "state_name",nullable = false)
    private String stateName;

    @Column(name = "country_name",nullable = false)
    private String countryName;

    @Column(name = "pin_code",nullable = false)
    private String pinCode;

    @Column(name = "type_of_address")
    private String typeOfAddress;

    @Column(name = "land_mark")
    private String landMark;

    @Column(name = "alt_phone")
    private String altPhone;

    @Column(name = "is_deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private UserEntity userEntity;

    @OneToMany(mappedBy = "addressEntity", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    private List<OrderEntity> orderEntities;

    @OneToMany(mappedBy = "addressEntity", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    private List<SlotEntity> slotEntities;
}