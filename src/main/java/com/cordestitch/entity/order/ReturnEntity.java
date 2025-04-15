package com.cordestitch.entity.order;

import com.cordestitch.enums.ReturnStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "return_order_data")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnEntity {

    @Id
    @Column(name = "return_id", nullable = false, unique = true)
    private String returnId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "order_item_id", nullable = false)
    private String orderItemId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

    @Column(name = "return_reason", nullable = false)
    private String returnReason;

    @Column(name = "return_type", nullable = false)
    private String returnType;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_status", nullable = false)
    private ReturnStatus returnStatus;

}
