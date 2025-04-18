package com.cordestitch.entity.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "user_bank_account_data")
@Getter
@Setter
@ToString
public class UserBankAccountEntity {

    @Id
    @Column(name = "user_bank_id", nullable = false)
    private String userBankId;

    @Column(name = "account_holder_name", nullable = false)
    private String accountHolderName;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "bank_ifsc_code", nullable = false)
    private String bankIfscCode;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "razorpay_contact_id", nullable = false)
    private String razorpayContactId;

    @Column(name = "razorpay_fund_account_id", nullable = false, unique = true)
    private String razorpayFundAccountId;

    @Column(name = "is_bank_account_deleted", nullable = false)
    private Boolean isBankAccountDeleted = false;

    @ManyToOne
    @JoinColumn(name = "user_id",referencedColumnName = "user_id", nullable = false)
    @ToString.Exclude
    private UserEntity userEntity;
}
