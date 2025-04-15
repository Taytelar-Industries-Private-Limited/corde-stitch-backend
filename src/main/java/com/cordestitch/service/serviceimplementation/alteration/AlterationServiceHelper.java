package com.cordestitch.service.serviceimplementation.alteration;

import com.cordestitch.entity.alteration.SlotEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.product.Product;
import com.cordestitch.entity.product.ProductImage;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.exception.alteration.DateConversionException;
import com.cordestitch.exception.alteration.MultipleAddressesFoundException;
import com.cordestitch.exception.order.OrderNotFoundException;
import com.cordestitch.repository.alteration.SlotRepository;
import com.cordestitch.repository.order.OrderItemRepository;
import com.cordestitch.repository.product.ProductRepository;
import com.cordestitch.request.alteration.GetSlotTimesRequest;
import com.cordestitch.response.alteration.BookedSlotResponse;
import com.cordestitch.response.alteration.BookedSlotTimes;
import com.cordestitch.response.alteration.SlotAvailability;
import com.cordestitch.response.alteration.SlotTimes;
import com.cordestitch.response.order.OrderItemResponse;
import com.cordestitch.response.user.AddressResponse;
import com.cordestitch.util.Constants;
import com.cordestitch.util.PinCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class AlterationServiceHelper {

    private final ProductRepository productRepository;

    private final OrderItemRepository orderItemRepository;

    private final SlotRepository slotRepository;

    private final ModelMapper modelMapper;

    private static final String TIME_ZONE = "Asia/Kolkata";
    private static final String TIME_PATTERN_12 = "hh:mma";
    private static final String TIME_PATTERN_24 = "HH:mm";
    private static final DateTimeFormatter TIME_FORMATTER_24 = DateTimeFormatter.ofPattern(TIME_PATTERN_24);
    private static final DateTimeFormatter TIME_FORMATTER_12 = DateTimeFormatter.ofPattern(TIME_PATTERN_12);

    public List<OrderItemResponse> mapToOrderItemResponse(List<OrderItemEntity> orderItemEntities) {
        log.info("List of OrderItems: {} ", orderItemEntities);
        List<OrderItemResponse> response = new ArrayList<>();
        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            OrderItemResponse orderItemResponse = new OrderItemResponse();
            orderItemResponse.setOrderItemId(orderItemEntity.getOrderItemId());

            Optional<Product> productOptional = productRepository.findByProductId(orderItemEntity.getProductId());
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                String productColor = orderItemEntity.getProductColor();
                String firstImageUrl = product.getProductImages().stream()
                        .filter(productImage -> productImage.getColorName().equalsIgnoreCase(productColor))
                        .sorted(Comparator.comparingInt(ProductImage::getImagePriority))
                        .map(ProductImage::getImageUrl)
                        .findFirst()
                        .orElse(null);
                orderItemResponse.setProductName(product.getProductName());
                orderItemResponse.setProductImage(firstImageUrl);
                orderItemResponse.setProductDescription(product.getProductDescription());
            }
            orderItemResponse.setProductId(orderItemEntity.getProductId());
            orderItemResponse.setQuantity(orderItemEntity.getQuantity());
            orderItemResponse.setUnitPrice(orderItemEntity.getUnitPrice());
            orderItemResponse.setTotalAmount(orderItemEntity.getTotalAmount());
            orderItemResponse.setProductColor(orderItemEntity.getProductColor());
            orderItemResponse.setReturnDaysPolicy(orderItemEntity.getReturnDaysPolicy());
            orderItemResponse.setProductOfferPercentage(orderItemEntity.getProductOfferPercentage());
            orderItemResponse.setDeliveryDate(orderItemEntity.getDeliveryDate());
            orderItemResponse.setReturnStatus(orderItemEntity.getReturnStatus());
            orderItemResponse.setDeliveryStatus(orderItemEntity.getDeliveryStatus());
            orderItemResponse.setCancelOrderDate(orderItemEntity.getCancelDate());
            orderItemResponse.setProductSize(orderItemEntity.getProductSize().toString());
            orderItemResponse.setOrderStatus(orderItemEntity.getOrderStatus());

            boolean isInBengaluru = PinCodes.isPinCodeInBengaluru(orderItemEntity.getOrderEntity().getAddressEntity().getPinCode());
            orderItemResponse.setPinCodeInBengaluru(isInBengaluru);

            response.add(orderItemResponse);
        }
        return response;
    }


    public SlotTimes mapToGetSlotTimes(GetSlotTimesRequest request) {
        if (request.getOrderItemIds() != null && !request.getOrderItemIds().isEmpty()) {
            log.info("GetSlotTimes Request : {}", request.getOrderItemIds());

            List<OrderItemEntity> orderItems = getOrderItemsByIds(request.getOrderItemIds());
            Map<String, List<OrderItemEntity>> orderItemsByAddress = groupOrderItemsByAddress(orderItems);
            validateSingleAddress(orderItemsByAddress);
            List<OrderItemEntity> itemsAtSameAddress = orderItemsByAddress.values().stream().findFirst().orElseThrow();
            log.info("Items at the same address: {}", itemsAtSameAddress);
        }
        LocalDate slotDate = request.getSlotDate() != null ? request.getSlotDate() : LocalDate.now();
        return getAvailableSlotTimes(slotDate);
    }

    public List<OrderItemEntity> getOrderItemsByIds(List<String> orderItemIds) {
        List<OrderItemEntity> orderItems = orderItemRepository.findByOrderItemIdIn(orderItemIds);
        if (orderItems.isEmpty()) {
            throw new OrderNotFoundException(Constants.ORDER_ITEM_NOT_FOUND);
        }
        return orderItems;
    }

    public Map<String, List<OrderItemEntity>> groupOrderItemsByAddress(List<OrderItemEntity> orderItems) {
        return orderItems.stream().collect(Collectors.groupingBy(orderItem -> {
            AddressEntity address = orderItem.getOrderEntity().getAddressEntity();
            return String.join(",",
                    address.getBuildingName(),
                    address.getStreetName(),
                    address.getCityName(),
                    address.getStateName(),
                    address.getPinCode(),
                    address.getCountryName(),
                    address.getTypeOfAddress());
        }));
    }

    public void validateSingleAddress(Map<String, List<OrderItemEntity>> orderItemsByAddress) {
        List<String> addresses = new ArrayList<>(orderItemsByAddress.keySet());
        if (addresses.size() > 1) {
            List<String> uniqueAddresses = addresses.stream().distinct().toList();
            if (uniqueAddresses.size() > 1) {
                log.error(Constants.MULTIPLE_ADDRESS_FOUND + "{}", uniqueAddresses);
                throw new MultipleAddressesFoundException(Constants.SELECT_ERROR_MESSAGE);
            }
        }
    }

    private SlotTimes getAvailableSlotTimes(LocalDate slotDate) {
        List<SlotEntity> bookedSlots = slotRepository.findBySlotDate(slotDate);
        List<SlotAvailability> availableSlots = generateAvailableTimeSlots();

        log.info("Booked Slots: {}", bookedSlots);
        LocalTime currentTime = LocalTime.now(ZoneId.of(TIME_ZONE));

        for (SlotAvailability slot : availableSlots) {
            updateSlotAvailability(slot, slotDate, currentTime);
        }

        markBookedSlotsAsUnavailable(availableSlots, bookedSlots);
        availableSlots = formatAvailableTimeSlots(availableSlots);
        log.info("Booked Slot Time: {}", availableSlots);
        return new SlotTimes(slotDate, availableSlots);
    }

    private List<SlotAvailability> formatAvailableTimeSlots(List<SlotAvailability> availableSlots) {
        List<SlotAvailability> list = new ArrayList<>();
        for (SlotAvailability availableSlot : availableSlots) {
            String updatedSlotTime = formatSlotTime(availableSlot.getSlotTime());
            availableSlot.setSlotTime(updatedSlotTime);
            list.add(availableSlot);
        }
        return list;
    }

    private String formatSlotTime(String slotTime) {
        String[] times = slotTime.split("-");
        String startTime = convertTo12HourFormat(times[0].trim());
        String endTime = convertTo12HourFormat(times[1].trim());
        return startTime + "-" + endTime;
    }

    public String convertTo12HourFormat(String time) {
        try {
            LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER_24);
            return localTime.format(TIME_FORMATTER_12).toUpperCase();
        } catch (DateTimeParseException e) {
            try {
                LocalTime localTime = LocalTime.parse(time.toUpperCase(), TIME_FORMATTER_12);
                return localTime.format(TIME_FORMATTER_12).toUpperCase();
            } catch (DateTimeParseException ex) {
                throw new DateConversionException("Invalid time format: " + time);
            }
        }
    }


    private void updateSlotAvailability(SlotAvailability slot, LocalDate slotDate, LocalTime currentTime) {
        String[] timeRange = slot.getSlotTime().split("-");
        LocalTime slotStartTime = LocalTime.parse(timeRange[0].trim(), TIME_FORMATTER_24);
        LocalTime slotEndTime = LocalTime.parse(timeRange[1].trim(), TIME_FORMATTER_24);

        if (slotDate.equals(LocalDate.now())) {
            slot.setAvailable(isSlotAvailableToday(slotStartTime, slotEndTime, currentTime));
        } else {
            slot.setAvailable(true);
        }
    }

    public boolean isSlotAvailableToday(LocalTime slotStartTime, LocalTime slotEndTime, LocalTime currentTime) {
        if (currentTime.isAfter(slotEndTime)) {
            return false;
        }
        return !(currentTime.isAfter(slotStartTime) && currentTime.isBefore(slotEndTime));
    }

    private void markBookedSlotsAsUnavailable(List<SlotAvailability> availableSlots, List<SlotEntity> bookedSlots) {
        for (SlotEntity bookedSlot : bookedSlots) {
            String bookedSlotTime = getFormattedSlotTime(bookedSlot);
            availableSlots.stream()
                    .filter(slot -> slot.getSlotTime().equalsIgnoreCase(bookedSlotTime))
                    .forEach(slot -> slot.setAvailable(false));
        }
    }

    private String getFormattedSlotTime(SlotEntity slotEntity) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN_24);
        return slotEntity.getStartTime().format(timeFormatter).toUpperCase() + "-" + slotEntity.getEndTime().format(timeFormatter).toUpperCase();
    }

    private List<SlotAvailability> generateAvailableTimeSlots() {
        List<SlotAvailability> availableSlots = new ArrayList<>();

        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(17, 0);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN_24);
        while (startTime.isBefore(endTime)) {
            LocalTime nextTime = startTime.plusHours(1);
            if (startTime.equals(LocalTime.of(13, 0))) {
                startTime = nextTime;
                continue;
            }

            String formattedSlotTime = startTime.format(timeFormatter).toUpperCase()
                    + "-" + nextTime.format(timeFormatter).toUpperCase();

            availableSlots.add(new SlotAvailability(formattedSlotTime, true));
            startTime = nextTime;
        }
        return availableSlots;
    }

    public String getSlotStartAndEndTime(SlotEntity slotEntity) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN_12);
        return slotEntity.getStartTime().format(timeFormatter).toUpperCase() + "-" + slotEntity.getEndTime().format(timeFormatter).toUpperCase();
    }

    public String getOrderItemIds(List<OrderItemEntity> orderItems) {

        return orderItems.size() == 1
                ? orderItems.get(0).getOrderItemId()
                : orderItems.stream()
                .map(OrderItemEntity::getOrderItemId)
                .collect(Collectors.joining(", "));
    }

    public List<OrderItemEntity> fetchOrderItemEntitiesBySlotId(String slotId) {
        return orderItemRepository.findBySlotEntitySlotId(slotId);
    }

    public BookedSlotResponse mapToBookedSlotTimes(String userId) {
        log.info("Get Booked Slot Times request : {}", userId);
        List<SlotEntity> slotEntities = slotRepository.findAllByUserEntityUserId(userId);

        if (!slotEntities.isEmpty()) {
            List<BookedSlotTimes> bookedSlotTimesList = new ArrayList<>();

            for (SlotEntity slotEntity : slotEntities) {
                List<OrderItemEntity> orderItems = fetchOrderItemEntitiesBySlotId(slotEntity.getSlotId());
                AddressResponse addressResponse = modelMapper.map(slotEntity.getAddressEntity(), AddressResponse.class);

                BookedSlotTimes bookedSlotTimes = new BookedSlotTimes();
                bookedSlotTimes.setSlotId(slotEntity.getSlotId());
                bookedSlotTimes.setBookedSlotDate(slotEntity.getSlotDate());
                bookedSlotTimes.setStartTime(slotEntity.getStartTime());
                bookedSlotTimes.setEndTime(slotEntity.getEndTime());
                bookedSlotTimes.setAddressResponse(addressResponse);
                bookedSlotTimes.setOrderItemResponses(mapToOrderItemResponse(orderItems));
                bookedSlotTimes.setIsNewMeasurement(slotEntity.getIsNewMeasurement());
                bookedSlotTimesList.add(bookedSlotTimes);
            }
            BookedSlotResponse response = new BookedSlotResponse();
            response.setBookedSlotTimes(bookedSlotTimesList);

            log.info("BookedSlotTimes Response : {}", response);
            return response;
        } else {
            log.info(Constants.CURRENTLY_DOES_NOT_BOOKED_ANY_SLOTS + ": {}", userId);
            return new BookedSlotResponse(new ArrayList<>());
        }
    }
}