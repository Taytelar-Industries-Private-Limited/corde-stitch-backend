package com.cordestitch.repository.user;

import com.cordestitch.entity.user.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity,String> {

    List<AddressEntity> findByUserEntityUserIdAndIsDeletedFalse(String userId);

    @Query("SELECT a FROM AddressEntity a WHERE a.userEntity.userId = :userId AND a.addressId = :addressId AND a.isDeleted = false")
    AddressEntity findByUserEntityUserIdAndAddressId(@Param("userId") String userId, @Param("addressId") String addressId);

    Optional<AddressEntity> findByAddressId(String addressId);
}