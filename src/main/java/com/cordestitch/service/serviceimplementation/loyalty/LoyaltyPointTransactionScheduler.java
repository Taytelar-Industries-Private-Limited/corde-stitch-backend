package com.cordestitch.service.serviceimplementation.loyalty;

import com.cordestitch.entity.loyalty.LoyaltyPointsEntity;
import com.cordestitch.entity.loyalty.LoyaltyPointsTransactionEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.enums.DeliveryStatus;
import com.cordestitch.enums.LoyaltyTransactionStatus;
import com.cordestitch.enums.ReturnStatus;
import com.cordestitch.exception.order.OrderItemNotFoundException;
import com.cordestitch.exception.review.ResourceNotFoundException;
import com.cordestitch.repository.loyalty.LoyaltyPointsRepository;
import com.cordestitch.repository.loyalty.LoyaltyPointsTransactionRepository;
import com.cordestitch.repository.order.OrderItemRepository;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

@Slf4j
@RequiredArgsConstructor
@Component
public class LoyaltyPointTransactionScheduler {

    private final LoyaltyPointsRepository loyaltyPointsRepository;

    private final OrderItemRepository orderItemRepository;

    private final LoyaltyPointsTransactionRepository loyaltyPointsTransactionRepository;


    public List<SuccessResponse> processLoyaltyPointsAfterReturnPeriod() {
        List<SuccessResponse> responses = new ArrayList<>();

        List<LoyaltyPointsTransactionEntity> pendingTransactions = loyaltyPointsTransactionRepository.findByLoyaltyTransactionStatus(LoyaltyTransactionStatus.PENDING);
        log.info("Loyalty Points Transaction Data : {}", pendingTransactions);

        for (LoyaltyPointsTransactionEntity transaction : pendingTransactions) {
            OrderItemEntity orderItem = orderItemRepository.findById(transaction.getOrderItemId())
                    .orElseThrow(() ->new OrderItemNotFoundException(Constants.ORDER_ITEM_NOT_FOUND));

            if (isEligibleForLoyaltyPointsConfirmation(orderItem)) {
                confirmLoyaltyPoints(transaction);
                responses.add(new SuccessResponse(Constants.LOYALTY_POINTS_CREDITED, HttpStatus.OK.value()));
            } else if (isOrderItemReturned(orderItem)) {
                cancelLoyaltyPoints(transaction);
                responses.add(new SuccessResponse(Constants.LOYALTY_POINTS_REJECTED, HttpStatus.OK.value()));
            }
        }
        return responses;
    }

    private boolean isEligibleForLoyaltyPointsConfirmation(OrderItemEntity orderItem) {
        LocalDateTime currentDate = LocalDateTime.now(ZoneId.of(Constants.ZONE));
        return orderItem.getDeliveryStatus() == DeliveryStatus.DELIVERED
                && orderItem.getReturnStatus() == ReturnStatus.NOT_RETURNED
                && currentDate.isAfter(
                orderItem.getDeliveryDate().plusDays(orderItem.getReturnDaysPolicy())
        );
    }

    private boolean isOrderItemReturned(OrderItemEntity orderItem) {
        return orderItem.getReturnStatus() == ReturnStatus.REFUND_COMPLETED;
    }


    private void confirmLoyaltyPoints(LoyaltyPointsTransactionEntity transaction) {

        LoyaltyPointsEntity loyaltyPointsEntity = loyaltyPointsRepository.findByLoyaltyId(transaction.getLoyaltyPointsEntity().getLoyaltyId());

        if (isNull(loyaltyPointsEntity)) {
           throw new ResourceNotFoundException(Constants.LOYALTY_POINTS_NOT_FOUND);
        }

        loyaltyPointsEntity.setTotalLoyaltyPoints(loyaltyPointsEntity.getTotalLoyaltyPoints() + transaction.getPointsChange());
        loyaltyPointsEntity.setLastUpdated(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        loyaltyPointsRepository.save(loyaltyPointsEntity);

        transaction.setLoyaltyTransactionStatus(LoyaltyTransactionStatus.CREDITED);
        transaction.setTransactionDate(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        loyaltyPointsTransactionRepository.save(transaction);
    }

    private void cancelLoyaltyPoints(LoyaltyPointsTransactionEntity transaction) {
        transaction.setLoyaltyTransactionStatus(LoyaltyTransactionStatus.CANCELED);
        loyaltyPointsTransactionRepository.save(transaction);
    }
}
