package com.cordestitch.service.serviceimplementation.whatsapp;

import com.cordestitch.request.whatsapp.*;
import com.cordestitch.util.WhatsAppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class WhatsAppInteractiveHelper {

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

    public List<InteractiveButtonRequest> getCancellationButtons(String payLoadData, String yesButton, String noButton) {
        String buttonIdForYes = "yes_" + payLoadData;
        String buttonIdForNo = "no_" + payLoadData;
        return Arrays.asList(
                createButton(buttonIdForYes, yesButton),
                createButton(buttonIdForNo, noButton));
    }
}