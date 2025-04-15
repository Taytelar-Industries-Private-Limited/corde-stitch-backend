package com.cordestitch.service.serviceimplementation.alteration;

import com.cordestitch.entity.alteration.SlotEntity;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.enums.DeliveryStatus;
import com.cordestitch.exception.alteration.SlotAlreadyBookedException;
import com.cordestitch.exception.alteration.SlotDataNotFoundException;
import com.cordestitch.exception.user.AddressNotFoundException;
import com.cordestitch.exception.user.UserDetailsMissMatchException;
import com.cordestitch.exception.user.UserNotFoundException;
import com.cordestitch.repository.alteration.SlotRepository;
import com.cordestitch.repository.order.OrderItemRepository;
import com.cordestitch.repository.order.OrderRepository;
import com.cordestitch.repository.user.AddressRepository;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.alteration.BookMeasurementSlotRequest;
import com.cordestitch.request.alteration.BookSlotRequest;
import com.cordestitch.request.alteration.GetSlotTimesRequest;
import com.cordestitch.request.alteration.RescheduleOrCancelRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.alteration.*;
import com.cordestitch.service.service.alteration.AlterationService;
import com.cordestitch.service.service.whatsapp.WhatsAppService;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import com.cordestitch.util.PinCodes;
import com.cordestitch.util.WhatsAppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlterationServiceImplementation implements AlterationService {

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final OrderItemRepository orderItemRepository;

    private final SlotRepository slotRepository;

    private final AddressRepository addressRepository;

    private final Generator generator;

    private final WhatsAppService whatsAppService;

    private final AlterationServiceHelper alterationServiceHelper;

    private static final String TIME_PATTERN = "HH:mm";

    @Value("${google.maps.api.key}")
    private String apiKey;

    @Value("${google.maps.url}")
    private String mapUrl;

    @Override
    public AlterationResponse getOrderItems(String userId, String phoneNumber, String emailAddress) {
        log.info("Get OrderItems Request userId: {}, PhoneNumber: {}, EmailAddress: {}", userId, phoneNumber, emailAddress);

        UserEntity userEntity = validateUserEntity(userId, phoneNumber, emailAddress);

        List<OrderEntity> orderEntities = orderRepository.findAllByUserEntityUserId(userEntity.getUserId());
        if (orderEntities.isEmpty()) {
            return new AlterationResponse();
        }

        List<OrderItemEntity> validOrderItems = getValidOrderItems(orderEntities);

        AlterationResponse response = new AlterationResponse();
        response.setOrderItemResponses(alterationServiceHelper.mapToOrderItemResponse(validOrderItems));
        log.info("Alteration Response : {}", response);
        return response;
    }

    @Override
    public SlotTimes getSlotTimes(GetSlotTimesRequest request) {
        return alterationServiceHelper.mapToGetSlotTimes(request);
    }

    @Transactional
    @Override
    public SuccessResponse bookSlotTimes(BookSlotRequest request) {
        log.info("BookSlotTimes Request: {}", request);
        TimeResponse timeResponse = mapToFormatTime(request.getStartTime(), request.getEndTime());
        log.info("While Book a Slot Formatted Time Response: {}", timeResponse);

        UserEntity userEntity = userRepository.findUserByUserId(request.getUserId());
        if (isNull(userEntity)) {
            throw new UserNotFoundException(Constants.USER_NOT_FOUND);
        }

        List<OrderItemEntity> orderItems = alterationServiceHelper.getOrderItemsByIds(request.getOrderItemIds());
        Map<String, List<OrderItemEntity>> orderItemsByAddress = alterationServiceHelper.groupOrderItemsByAddress(orderItems);
        alterationServiceHelper.validateSingleAddress(orderItemsByAddress);
        List<OrderItemEntity> itemsAtSameAddress = orderItemsByAddress.values().stream().findFirst().orElseThrow();
        AddressEntity addressEntity = itemsAtSameAddress.getFirst().getOrderEntity().getAddressEntity();

        SlotEntity slotEntity = new SlotEntity();
        slotEntity.setSlotId(generator.generateId(Constants.SLOT_ID));
        slotEntity.setSlotDate(request.getSlotDate());
        slotEntity.setStartTime(timeResponse.getStartTime());
        slotEntity.setEndTime(timeResponse.getEndTime());
        slotEntity.setSlotBooked(true);
        slotEntity.setIsNewMeasurement(false);
        slotEntity.setUserEntity(userEntity);
        slotEntity.setAddressEntity(addressEntity);
        for (OrderItemEntity orderItem : orderItems) {
            orderItem.setSlotEntity(slotEntity);
        }
        slotEntity.setOrderItems(orderItems);
        slotRepository.save(slotEntity);
        log.info("Slot Entity saved : {}", slotEntity);

        String orderItem = alterationServiceHelper.getOrderItemIds(orderItems);
        String slotTime = alterationServiceHelper.getSlotStartAndEndTime(slotEntity);
        whatsAppService.sendAlterationAppointmentMessage(userEntity.getPhoneNumber(), userEntity.getFirstName(), orderItem, slotEntity.getSlotDate().toString(), slotTime);

        return new SuccessResponse(Constants.SLOT_BOOKED_SUCCESSFULLY, HttpStatus.OK.value());
    }

    @Transactional
    @Override
    public SuccessResponse rescheduleOrCancelSlotTimes(RescheduleOrCancelRequest request) {
        log.info("Cancel slot times request: {}", request);
        TimeResponse time = mapToFormatTime(request.getStartTime(), request.getEndTime());

        Optional<SlotEntity> slotEntityOptional = slotRepository.findByUserEntityUserIdAndSlotDateAndStartTimeAndEndTime(request.getUserId(), request.getSlotDate(), time.getStartTime(), time.getEndTime());
        if (slotEntityOptional.isPresent()) {
            SlotEntity slotEntity = slotEntityOptional.get();
            UserEntity userEntity = slotEntity.getUserEntity();
            String oldSlotTime = alterationServiceHelper.getSlotStartAndEndTime(slotEntity);
            if (Boolean.TRUE.equals(request.getReschedule())) {
                TimeResponse timeResponse = mapToFormatTime(request.getNewStartTime(), request.getNewEndTime());
                log.info("While Reschedule Formatted Time Response: {}", timeResponse);
                slotEntity.setSlotDate(request.getNewSlotDate());
                slotEntity.setStartTime(timeResponse.getStartTime());
                slotEntity.setEndTime(timeResponse.getEndTime());
                slotRepository.save(slotEntity);

                String newSlotTime = alterationServiceHelper.getSlotStartAndEndTime(slotEntity);

                String orderItemId = slotEntity.getOrderItems().isEmpty()
                        ? WhatsAppConstants.RESCHEDULE_FIT_APPOINTMENT_TEXT
                        : WhatsAppConstants.RESCHEDULE_ALTERATION_APPOINTMENT_TEXT + slotEntity.getOrderItems().getFirst().getOrderItemId();
                whatsAppService.sendAppointmentRescheduleMessage(userEntity.getPhoneNumber(), userEntity.getFirstName(), orderItemId,
                        request.getSlotDate().toString(), oldSlotTime,
                        request.getNewSlotDate().toString(), newSlotTime);

                return new SuccessResponse(Constants.SLOT_TIME_RESCHEDULED_SUCCESSFULLY, HttpStatus.OK.value());
            } else {
                List<OrderItemEntity> orderItemEntities = alterationServiceHelper.fetchOrderItemEntitiesBySlotId(slotEntity.getSlotId());
                log.info("Order Items associated with the slotId : {}", orderItemEntities);

                orderItemEntities
                        .forEach(orderItem -> {
                            orderItem.setSlotEntity(null);
                            orderItemRepository.save(orderItem);
                        });

                slotRepository.delete(slotEntity);
                log.info("Deleted Slot Entity : {}", slotEntity);

                String slotTime = alterationServiceHelper.getSlotStartAndEndTime(slotEntity);
                whatsAppService.sendAppointmentCancellationMessage(userEntity.getPhoneNumber(), userEntity.getFirstName(), slotEntity.getSlotDate().toString(), slotTime);
                return new SuccessResponse(Constants.CANCELLED_SLOT_TIME_SUCCESSFULLY, HttpStatus.OK.value());
            }
        } else {
            log.error(Constants.SLOT_DATA_NOT_FOUND, request.getSlotDate(), request.getStartTime(), request.getEndTime());
            throw new SlotDataNotFoundException(String.format(Constants.SLOT_DATA_NOT_FOUND, request.getSlotDate(), request.getStartTime(), request.getEndTime()));
        }
    }

    @Override
    public BookedSlotResponse getBookedSlotTimes(String userId) {
        return alterationServiceHelper.mapToBookedSlotTimes(userId);
    }

    @Override
    public SuccessResponse bookMeasurementSlot(BookMeasurementSlotRequest request) {
        log.info("Book measurement slot request: {}", request);
        TimeResponse timeResponse = mapToFormatTime(request.getStartTime(), request.getEndTime());
        log.info("While New Measurement Formatted Time Response: {}", timeResponse);

        UserEntity userEntity = userRepository.findUserByUserId(request.getUserId());
        log.info("User Data: {}", userEntity);
        if (isNull(userEntity)) {
            throw new UserNotFoundException(Constants.USER_NOT_FOUND + ":{}" + request.getUserId());
        }

        Optional<SlotEntity> optionalSlot = slotRepository.findBySlotDateAndStartTimeAndEndTime(request.getSlotDate(), timeResponse.getStartTime(), timeResponse.getEndTime());
        log.info("Checking Slot Entity Data : {}", optionalSlot);
        if (optionalSlot.isPresent()) {
            throw new SlotAlreadyBookedException(String.format(Constants.SLOT_ALREADY_BOOKED, request.getSlotDate(), request.getStartTime(), request.getEndTime()));
        }

        Optional<AddressEntity> optionalAddress = addressRepository.findByAddressId(request.getAddressId());
        log.info("Address Data: {}", optionalAddress);
        if (optionalAddress.isEmpty()) {
            throw new AddressNotFoundException(Constants.ADDRESS_NOT_FOUND);
        }

        if (!validateAddressByPinCode(optionalAddress.get().getPinCode())) {
            return new SuccessResponse(Constants.PIN_CODE_ERR_MSG, HttpStatus.BAD_REQUEST.value());
        }

        DateTimeFormatter standardFormat = DateTimeFormatter.ofPattern(TIME_PATTERN);

        SlotEntity slotEntity = new SlotEntity();
        slotEntity.setSlotId(generator.generateId(Constants.SLOT_ID));
        slotEntity.setSlotDate(request.getSlotDate());
        slotEntity.setStartTime(LocalTime.parse(timeResponse.getStartTime().format(standardFormat)));
        slotEntity.setEndTime(LocalTime.parse(timeResponse.getEndTime().format(standardFormat)));
        slotEntity.setSlotBooked(true);
        slotEntity.setUserEntity(userEntity);
        slotEntity.setAddressEntity(optionalAddress.get());
        slotEntity.setIsNewMeasurement(true);
        slotRepository.save(slotEntity);

        String slotTime = alterationServiceHelper.getSlotStartAndEndTime(slotEntity);
        whatsAppService.sendFitAppointmentMessage(userEntity.getPhoneNumber(), userEntity.getFirstName(), slotEntity.getSlotDate().toString(), slotTime);

        return new SuccessResponse(Constants.SLOT_BOOKED_SUCCESSFULLY, HttpStatus.OK.value());
    }

    private boolean validateAddressByPinCode(String pinCode) {
        return PinCodes.isPinCodeInBengaluru(pinCode);
    }

    private UserEntity validateUserEntity(String userId, String phoneNumber, String emailAddress) {
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            return userRepository.findByUserIdAndPhoneNumber(userId, phoneNumber)
                    .orElseThrow(() -> new UserNotFoundException(Constants.USER_NOT_FOUND));
        } else if (emailAddress != null && !emailAddress.isEmpty()) {
            return userRepository.findByUserIdAndEmailAddress(userId, emailAddress)
                    .orElseThrow(() -> new UserNotFoundException(Constants.USER_NOT_FOUND));
        } else {
            throw new UserDetailsMissMatchException(Constants.USER_DATA_MISSING_ERROR);
        }
    }

    private List<OrderItemEntity> getValidOrderItems(List<OrderEntity> filteredOrderEntities) {
        return filteredOrderEntities.stream()
                .flatMap(orderEntity -> orderEntity.getOrderItemEntities().stream()
                        .filter(orderItemEntity -> orderItemEntity.getSlotEntity() == null)
                        .filter(orderItemEntity -> DeliveryStatus.DELIVERED.equals(orderItemEntity.getDeliveryStatus()))
                        .filter(orderItemEntity -> !isReturnPolicyExceeded(orderItemEntity)))
                .toList();
    }

    private boolean isReturnPolicyExceeded(OrderItemEntity orderItemEntity) {
        LocalDateTime returnDeadLine = orderItemEntity.getDeliveryDate().plusDays(orderItemEntity.getReturnDaysPolicy());
        return LocalDateTime.now(ZoneId.of(Constants.ZONE)).isAfter(returnDeadLine);
    }

    private TimeResponse mapToFormatTime(LocalTime startTime, LocalTime endTime) {

        if (startTime.getHour() >= 1 && startTime.getHour() <= 9) {
            startTime = startTime.plusHours(12);
        }
        if (endTime.getHour() >= 1 && endTime.getHour() <= 9) {
            endTime = endTime.plusHours(12);
        }

        DateTimeFormatter standardFormat = DateTimeFormatter.ofPattern(TIME_PATTERN);
        String standardizedStartTime = startTime.format(standardFormat);
        String standardizedEndTime = endTime.format(standardFormat);

        log.info("Converted times - Original Start: {} -> Converted: {}, Original End: {} -> Converted: {}",
                startTime, standardizedStartTime,
                endTime, standardizedEndTime);
        return new TimeResponse(startTime, endTime);
    }
}