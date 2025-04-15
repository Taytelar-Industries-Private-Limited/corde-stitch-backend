package com.cordestitch.service.serviceimplementation.whatsapp;

import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.alteration.GetSlotTimesRequest;
import com.cordestitch.request.whatsapp.*;
import com.cordestitch.response.alteration.BookedSlotResponse;
import com.cordestitch.response.alteration.SlotAvailability;
import com.cordestitch.response.alteration.SlotTimes;
import com.cordestitch.response.order.OrderItemResponse;
import com.cordestitch.service.serviceimplementation.alteration.AlterationServiceHelper;
import com.cordestitch.util.Constants;
import com.cordestitch.util.WhatsAppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

@Component
@Slf4j
@RequiredArgsConstructor
public class WhatsAppInteractiveHelper {

    private final AlterationServiceHelper alterationServiceHelper;

    private final UserRepository userRepository;

    public WhatsAppMessageRequest getInteractiveMessageRequest(String recipientNumber, String headerText, String bodyText, String buttonText, String interActiveType, List<SectionRequest> sections, List<InteractiveButtonRequest> buttons) {
        WhatsAppMessageRequest whatsAppMessageRequest = new WhatsAppMessageRequest();
        whatsAppMessageRequest.setMessagingProduct(WhatsAppConstants.WHATSAPP);
        whatsAppMessageRequest.setTo(recipientNumber);
        whatsAppMessageRequest.setType(WhatsAppConstants.INTERACTIVE);

        if (interActiveType.equals(WhatsAppConstants.LIST)) {
            InteractiveRequest interactive = createInteractiveList(headerText, bodyText, buttonText, interActiveType, sections);
            whatsAppMessageRequest.setInteractive(interactive);
        } else if (interActiveType.equals(WhatsAppConstants.BUTTON)) {
            InteractiveRequest interactive = createInteractiveButton(bodyText, interActiveType, buttons);
            whatsAppMessageRequest.setInteractive(interactive);
        }

        return whatsAppMessageRequest;
    }


    public InteractiveRequest createInteractiveList(String headerText, String bodyText, String buttonText, String interActiveType, List<SectionRequest> sections) {

        InteractiveRequest interactive = new InteractiveRequest();
        interactive.setType(interActiveType);

        HeaderRequest header = createHeaderRequest(headerText);
        interactive.setHeader(header);

        BodyRequest body = createBodyRequest(bodyText);
        interactive.setBody(body);

        ActionRequest actionRequestForList = createActionRequestForList(buttonText, sections);
        interactive.setAction(actionRequestForList);

        return interactive;
    }

    public InteractiveRequest createInteractiveButton(String bodyText, String interActiveType, List<InteractiveButtonRequest> buttons) {

        InteractiveRequest interactive = new InteractiveRequest();
        interactive.setType(interActiveType);

        BodyRequest body = createBodyRequest(bodyText);
        interactive.setBody(body);

        ActionRequest actionRequestForButton = createActionRequestForButton(buttons);
        interactive.setAction(actionRequestForButton);

        return interactive;
    }

    private ActionRequest createActionRequestForButton(List<InteractiveButtonRequest> buttons) {
        ActionRequest action = new ActionRequest();
        action.setButtons(buttons);

        return action;
    }

    public ActionRequest createActionRequestForList(String buttonText, List<SectionRequest> sections) {
        ActionRequest action = new ActionRequest();
        action.setButton(buttonText);
        action.setSections(sections);

        return action;
    }

    public HeaderRequest createHeaderRequest(String headerText) {
        HeaderRequest header = new HeaderRequest();
        header.setType(WhatsAppConstants.HEADER_TYPE);
        header.setText(headerText);

        return header;
    }

    public BodyRequest createBodyRequest(String bodyText) {
        BodyRequest body = new BodyRequest();
        body.setText(bodyText);

        return body;
    }

    public SectionRequest createSection(String title, List<RowRequest> rows) {
        SectionRequest section = new SectionRequest();
        section.setTitle(title);
        section.setRows(rows);

        return section;
    }

    public RowRequest createRow(String id, String title) {
        RowRequest row = new RowRequest();
        row.setId(id);
        row.setTitle(title);

        return row;
    }

    public List<SectionRequest> getCancellationReasonSections(String orderItemId) {
        List<RowRequest> rows = Arrays.asList(
                createRow("1_" + orderItemId, WhatsAppConstants.IN_APPROPRIATE_SIZE),
                createRow("2_" + orderItemId, WhatsAppConstants.ORDERED_BY_MISTAKE),
                createRow("3_" + orderItemId, WhatsAppConstants.CHANGED_MIND),
                createRow("4_" + orderItemId, WhatsAppConstants.FOUND_BETTER_PRICE),
                createRow("5_" + orderItemId, WhatsAppConstants.NO_LONGER_NEEDED),
                createRow("6_" + orderItemId, WhatsAppConstants.FOUND_OTHER),
                createRow("7_" + orderItemId, WhatsAppConstants.WRONG_COLOR),
                createRow("8_" + orderItemId, WhatsAppConstants.QUALITY_CONCERN)
        );

        SectionRequest section = createSection(WhatsAppConstants.TITTLE, rows);

        return Collections.singletonList(section);
    }


    public List<InteractiveButtonRequest> getCatalogAndSupportSections() {

        String buttonIdForProductCategory = "1_product_category";
        String buttonIdForTrending = "2_trending_product";
        String buttonIdForContact = "3_support_contact";

        return Arrays.asList(
                createButton(buttonIdForProductCategory, WhatsAppConstants.TOP_CATEGORY),
                createButton(buttonIdForTrending, WhatsAppConstants.TRENDING_PRODUCT),
                createButton(buttonIdForContact, WhatsAppConstants.CONTACT_US));
    }

    public List<InteractiveButtonRequest> getCancellationButtons(String payLoadData, String yesButton, String noButton) {
        String buttonIdForYes = "yes_" + payLoadData;
        String buttonIdForNo = "no_" + payLoadData;

        return Arrays.asList(
                createButton(buttonIdForYes, yesButton),
                createButton(buttonIdForNo, noButton));
    }

    public List<InteractiveButtonRequest> createWelcomeMessageButtons() {
        String buttonIdForYes = "start_button";
        String buttonIdForNo = "stop_button";

        return Arrays.asList(
                createButton(buttonIdForYes, WhatsAppConstants.START_BUTTON),
                createButton(buttonIdForNo, WhatsAppConstants.STOP_BUTTON));
    }

    public List<SectionRequest> createTopCategorySections() {
        List<RowRequest> rows = Arrays.asList(
                createRow("1_formal", WhatsAppConstants.FORMAL_PANTS),
                createRow("2_causal", WhatsAppConstants.CASUAL_PANTS),
                createRow("3_cargo", WhatsAppConstants.CARGO_PANTS)
        );
        SectionRequest section = createSection(WhatsAppConstants.TITTLE, rows);

        return Collections.singletonList(section);
    }

    private InteractiveButtonRequest createButton(String id, String title) {
        InteractiveButtonRequest buttonRequest = new InteractiveButtonRequest();
        buttonRequest.setType(WhatsAppConstants.REPLY);

        ReplyRequest reply = new ReplyRequest();
        reply.setId(id);
        reply.setTitle(title);

        buttonRequest.setReply(reply);
        return buttonRequest;
    }

    public List<String> fetchAvailableSlotTimes(String payLoadData) {

        LocalDate newSlotDate = extractDate(payLoadData, "_N_", "$");
        GetSlotTimesRequest request = new GetSlotTimesRequest();
        request.setSlotDate(newSlotDate);

        SlotTimes slotTimeResponse = alterationServiceHelper.mapToGetSlotTimes(request);

        return slotTimeResponse.getAvailableSlots().stream()
                .filter(SlotAvailability::isAvailable)
                .map(SlotAvailability::getSlotTime)
                .toList();

    }

    public List<LocalDate> fetchAvailableSlotDates(String recipientNumber, String payLoadData) {

        String phoneNumber = (recipientNumber.startsWith("91") && recipientNumber.length() > 10)
                ? recipientNumber.substring(2) : recipientNumber;

        String orderItemId = extractOrderItemId(payLoadData);
        UserEntity userEntity = userRepository.findUserByPhoneNumber(phoneNumber);

        return isNull(orderItemId)
                ? getAvailableFitTimeSlots(payLoadData)
                : getAvailableAlterationTimeSlots(orderItemId, userEntity);

    }

    private List<LocalDate> getAvailableFitTimeSlots(String payLoadData) {

        LocalDate oldSlotDate = Optional.ofNullable(extractDate(payLoadData, "t_", "_T_"))
                .orElseGet(() -> extractDate(payLoadData, "t _", "_T_"));
        LocalDate currentDate = LocalDate.now();

        LocalDate startDate = oldSlotDate.minusDays(3);
        LocalDate endDate = oldSlotDate.plusDays(3);

        if (startDate.isBefore(currentDate)) {
            int difference = (int) ChronoUnit.DAYS.between(startDate, currentDate);
            startDate = currentDate;
            endDate = endDate.plusDays(difference);
        }

        return startDate.datesUntil(endDate.plusDays(1)).toList();
    }


    private List<LocalDate> getAvailableAlterationTimeSlots(String orderItemId, UserEntity userEntity) {

        BookedSlotResponse bookedSlotResponse = alterationServiceHelper.mapToBookedSlotTimes(userEntity.getUserId());

        List<LocalDate> availableDates = new ArrayList<>();

        List<OrderItemResponse> orderItemResponses = bookedSlotResponse.getBookedSlotTimes().stream()
                .flatMap(orderResponse -> orderResponse.getOrderItemResponses().stream())
                .filter(orderItem -> orderItem.getOrderItemId().equals(orderItemId))
                .toList();

        for (OrderItemResponse orderItemResponse : orderItemResponses) {
            if (!isNull(orderItemResponse)) {
                LocalDate deliveredDate = orderItemResponse.getDeliveryDate().toLocalDate();
                int returnDaysPolicy = orderItemResponse.getReturnDaysPolicy();
                LocalDate currentDate = LocalDate.now();

                LocalDate returnExpiryDate = deliveredDate.plusDays(returnDaysPolicy);

                if (!currentDate.isAfter(returnExpiryDate)) {
                    for (LocalDate date = currentDate; !date.isAfter(returnExpiryDate); date = date.plusDays(1)) {
                        availableDates.add(date);
                    }
                }
            }
        }
        return availableDates;
    }

    private LocalDate extractDate(String input, String startDelimiter, String endDelimiter) {
        String regex = startDelimiter + "(\\d{4}-\\d{2}-\\d{2})" + endDelimiter;
        return extractLocalDate(input, regex);
    }

    private LocalDate extractLocalDate(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? LocalDate.parse(matcher.group(1)) : null;
    }

    private String extractOrderItemId(String orderItemId) {
        Pattern pattern = Pattern.compile(Constants.ORDER_ITEM_ID + "\\d+");
        Matcher matcher = pattern.matcher(orderItemId);
        log.info("Matching data {} ", matcher);

        return matcher.find() ? matcher.group(0) : null;
    }
}