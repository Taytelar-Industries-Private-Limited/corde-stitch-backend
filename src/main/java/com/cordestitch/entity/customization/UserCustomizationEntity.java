package com.cordestitch.entity.customization;

import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.user.UserEntity;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_customization_data")
@Getter
@Setter
@ToString
public class UserCustomizationEntity {

    @Id
    @Column(name = "user_customization_id",nullable = false,unique = true)
    private String userCustomizationId;

    @Column(name = "pant_type",nullable = false)
    private String pantType;

    @Column(name = "true_waist_measurement",nullable = false)
    private Integer trueWaistMeasurement;

    @Column(name = "pant_in_seam_length",nullable = false)
    private Integer pantInSeamLength;

    @Column(name = "pant_out_seam_length",nullable = false)
    private Integer pantOutSeamLength;

    @Column(name = "fit_type",nullable = false)
    private String fitType;

    @Column(name = "rise_type",nullable = false)
    private String riseType;

    @Column(name = "front_pocket_type",nullable = false)
    private String frontPocketType;

    @Column(name = "back_pocket_type",nullable = false)
    private String backPocketType;

    @Column(name = "front_button_type",nullable = false)
    private String frontButtonType;

    @Column(name = "back_button_type",nullable = false)
    private String backButtonType;

    @Column(name = "pant_pleat_type",nullable = false)
    private String pantPleatType;

    @Column(name = "fly_type",nullable = false)
    private String flyType;

    @Column(name = "pant_cuffs_type",nullable = false)
    private String pantCuffsType;

    @Column(name = "customization_product_url",nullable = false)
    private String customizationProductUrl;

    @Column(columnDefinition = "jsonb", name = "fabric_material",nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode fabric;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ToString.Exclude
    private UserEntity userEntity;

    @OneToOne(mappedBy = "userCustomizationEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private OrderItemEntity orderItemEntity;

}