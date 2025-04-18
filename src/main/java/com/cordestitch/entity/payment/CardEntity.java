package com.cordestitch.entity.payment;

import com.cordestitch.entity.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "card_data")
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CardEntity {

    @Id
    @Column(name = "card_id")
    private String cardId;

    @Column(name = "razor_pay_token")
    private String razorPayToken;

    @Column(name = "card_type")
    private String cardType;

    @Column(name = "card_last_four_digits")
    private String cardLastFourDigits;

    @Column(name = "card_expiration_date")
    private String expirationDate;

    @Column(name = "card_holder_name")
    private String cardHolderName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ToString.Exclude
    private UserEntity userEntity;

}