package com.cordestitch.serviceimplementation.whatsapp;

import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.whatsapp.*;
import com.cordestitch.service.serviceimplementation.whatsapp.WhatsAppInteractiveHelper;
import com.cordestitch.util.WhatsAppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WhatsAppInteractiveHelperTest {


    @InjectMocks
    @Spy
    private WhatsAppInteractiveHelper whatsAppInteractiveHelper;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createInteractiveMessageRequest_when_interactive_type_is_list() {
        WhatsAppMessageRequest expectedWhatsAppMessageRequest = getWhatsAppMessageRequest();
        List<SectionRequest> expectedSections = getInterActiveSections();
        WhatsAppMessageRequest whatsAppMessageRequest = whatsAppInteractiveHelper.getInteractiveMessageRequest("9148671766", "header text", "body Text", "button Text", "list", getInterActiveSections(), getInterActiveButtons());
        assertEquals(expectedWhatsAppMessageRequest.getMessagingProduct(), whatsAppMessageRequest.getMessagingProduct());
        assertEquals(expectedSections, whatsAppMessageRequest.getInteractive().getAction().getSections());
    }

    @Test
    void createInteractiveMessageRequest_when_interactive_type_is_Button() {
        WhatsAppMessageRequest expectedWhatsAppMessageRequest = getWhatsAppMessageRequest();
        List<InteractiveButtonRequest> expectedInteractiveButtonRequests = getInterActiveButtons();
        WhatsAppMessageRequest whatsAppMessageRequest = whatsAppInteractiveHelper.getInteractiveMessageRequest("9148671766", "header text", "body Text", "button Text", "button", getInterActiveSections(), getInterActiveButtons());
        assertEquals(expectedWhatsAppMessageRequest.getMessagingProduct(), whatsAppMessageRequest.getMessagingProduct());
        assertEquals(expectedInteractiveButtonRequests, whatsAppMessageRequest.getInteractive().getAction().getButtons());
    }

    @Test
    void createInteractiveMessageRequest_when_interactive_type_is_not_Button() {
        WhatsAppMessageRequest expectedWhatsAppMessageRequest = getWhatsAppMessageRequest();
        WhatsAppMessageRequest whatsAppMessageRequest = whatsAppInteractiveHelper.getInteractiveMessageRequest("9148671766", "header text", "body Text", "button Text", "reply", getInterActiveSections(), getInterActiveButtons());
        assertEquals(expectedWhatsAppMessageRequest.getMessagingProduct(), whatsAppMessageRequest.getMessagingProduct());
    }

    @Test
    void createCancellationReasonSections() {
        List<SectionRequest> expectedSections = getExpectedCancellationReasonSections();
        List<SectionRequest> sectionRequestList = whatsAppInteractiveHelper.getCancellationReasonSections("OD12345");
        assertEquals(expectedSections, sectionRequestList);
    }


    @Test
    void createCatalogAndSupportSections() {
        List<InteractiveButtonRequest> expectedInteractiveButtonRequestList = getExpectedCatalogAndSupportButtons();
        List<InteractiveButtonRequest> interactiveButtonRequestList = whatsAppInteractiveHelper.getCatalogAndSupportSections();
        assertEquals(expectedInteractiveButtonRequestList, interactiveButtonRequestList);
    }

    @Test
    void createCancellationButtons() {
        List<InteractiveButtonRequest> expectedInteractiveButtonRequestList = getExpectedCancellationButtons();
        List<InteractiveButtonRequest> interactiveButtonRequestList = whatsAppInteractiveHelper.getCancellationButtons("OD123", "Yes", "No");
        assertEquals(expectedInteractiveButtonRequestList, interactiveButtonRequestList);
    }

    @Test
    void createWelcomeMessageButtons() {
        List<InteractiveButtonRequest> expectedInteractiveButtonRequestList = getExpectedWelcomeMessageButtons();
        List<InteractiveButtonRequest> interactiveButtonRequestList = whatsAppInteractiveHelper.createWelcomeMessageButtons();
        assertEquals(expectedInteractiveButtonRequestList, interactiveButtonRequestList);
    }

    @Test
    void createTopCategorySections() {
        List<SectionRequest> expectedInteractiveButtonRequestList = getTopCategorySections();
        List<SectionRequest> interactiveButtonRequestList = whatsAppInteractiveHelper.createTopCategorySections();
        assertEquals(expectedInteractiveButtonRequestList, interactiveButtonRequestList);
    }

    private List<InteractiveButtonRequest> getExpectedCancellationButtons() {
        List<InteractiveButtonRequest> interactiveButtonRequestList = new ArrayList<>();
        interactiveButtonRequestList.add(getButtons("yes_OD123", WhatsAppConstants.YES_BUTTON));
        interactiveButtonRequestList.add(getButtons("no_OD123", WhatsAppConstants.NO_BUTTON));
        return interactiveButtonRequestList;
    }

    private List<InteractiveButtonRequest> getExpectedWelcomeMessageButtons() {
        List<InteractiveButtonRequest> interactiveButtonRequestList = new ArrayList<>();
        interactiveButtonRequestList.add(getButtons("start_button", WhatsAppConstants.START_BUTTON));
        interactiveButtonRequestList.add(getButtons("stop_button", WhatsAppConstants.STOP_BUTTON));
        return interactiveButtonRequestList;
    }

    private List<InteractiveButtonRequest> getExpectedCatalogAndSupportButtons() {
        List<InteractiveButtonRequest> interactiveButtonRequestList = new ArrayList<>();
        interactiveButtonRequestList.add(getButtons("1_product_category", WhatsAppConstants.TOP_CATEGORY));
        interactiveButtonRequestList.add(getButtons("2_trending_product", WhatsAppConstants.TRENDING_PRODUCT));
        interactiveButtonRequestList.add(getButtons("3_support_contact", WhatsAppConstants.CONTACT_US));
        return interactiveButtonRequestList;
    }

    private List<SectionRequest> getTopCategorySections() {
        List<RowRequest> rows = new ArrayList<>();
        rows.add(getSectionRequestRow("1_formal", WhatsAppConstants.FORMAL_PANTS));
        rows.add(getSectionRequestRow("2_causal", WhatsAppConstants.CASUAL_PANTS));
        rows.add(getSectionRequestRow("3_cargo", WhatsAppConstants.CARGO_PANTS));
        SectionRequest sectionRequest = new SectionRequest();
        sectionRequest.setTitle("tittle");
        sectionRequest.setRows(rows);
        return Collections.singletonList(sectionRequest);
    }

    private InteractiveButtonRequest getButtons(String id, String type) {
        InteractiveButtonRequest buttonRequest = new InteractiveButtonRequest();
        buttonRequest.setType("reply");
        buttonRequest.setReply(getButtonReply(id, type));
        return buttonRequest;
    }

    private ReplyRequest getButtonReply(String id, String type) {
        ReplyRequest replyRequest = new ReplyRequest();
        replyRequest.setId(id);
        replyRequest.setTitle(type);
        return replyRequest;
    }


    private List<SectionRequest> getExpectedCancellationReasonSections() {
        List<RowRequest> rows = new ArrayList<>();
        rows.add(getSectionRequestRow("1_" + "OD12345", "Inappropriate Size"));
        rows.add(getSectionRequestRow("2_" + "OD12345", "Order by Mistake"));
        rows.add(getSectionRequestRow("3_" + "OD12345", "Changed Mind"));
        rows.add(getSectionRequestRow("4_" + "OD12345", "Found a Better Price"));
        rows.add(getSectionRequestRow("5_" + "OD12345", "No Longer Needed"));
        rows.add(getSectionRequestRow("6_" + "OD12345", "Found Alternative"));
        rows.add(getSectionRequestRow("7_" + "OD12345", "Wrong Item/Color"));
        rows.add(getSectionRequestRow("8_" + "OD12345", "Product Quality Issues"));
        SectionRequest sectionRequest = new SectionRequest();
        sectionRequest.setTitle("tittle");
        sectionRequest.setRows(rows);
        return Collections.singletonList(sectionRequest);
    }

    private RowRequest getSectionRequestRow(String id, String reason) {
        RowRequest rowRequest = new RowRequest();
        rowRequest.setId(id);
        rowRequest.setTitle(reason);
        return rowRequest;
    }

    private InteractiveRequest getInterActiveList() {
        InteractiveRequest interactiveRequest = new InteractiveRequest();
        interactiveRequest.setType("list");
        interactiveRequest.setHeader(getInterActiveHeader());
        interactiveRequest.setBody(getInterActiveBody());
        interactiveRequest.setAction(getInterActiveAction());
        return interactiveRequest;
    }

    private ActionRequest getInterActiveAction() {
        ActionRequest actionRequest = new ActionRequest();
        actionRequest.setButton("button Text");
        actionRequest.setSections(getInterActiveSections());
        actionRequest.setButtons(getInterActiveButtons());
        return actionRequest;
    }

    private List<InteractiveButtonRequest> getInterActiveButtons() {
        List<InteractiveButtonRequest> interactiveButtonRequestList = new ArrayList<>();
        InteractiveButtonRequest buttonRequest = new InteractiveButtonRequest();
        buttonRequest.setType("button type");
        buttonRequest.setReply(getInterActiveButtonReplyRequest());
        interactiveButtonRequestList.add(buttonRequest);
        return interactiveButtonRequestList;
    }

    private ReplyRequest getInterActiveButtonReplyRequest() {
        ReplyRequest replyRequest = new ReplyRequest();
        replyRequest.setId("reply_id");
        replyRequest.setTitle("reply_Tittle");
        return replyRequest;
    }

    private List<SectionRequest> getInterActiveSections() {
        List<SectionRequest> sectionRequestList = new ArrayList<>();
        SectionRequest sectionRequest = new SectionRequest();
        sectionRequest.setTitle("section tittle");
        sectionRequest.setRows(getSectionRequestRows());
        sectionRequestList.add(sectionRequest);
        return sectionRequestList;
    }

    private List<RowRequest> getSectionRequestRows() {
        List<RowRequest> rowRequestList = new ArrayList<>();
        RowRequest rowRequest = new RowRequest();
        rowRequest.setId("row id");
        rowRequest.setTitle("row tittle");
        rowRequest.setDescription("row description");
        rowRequestList.add(rowRequest);
        return rowRequestList;
    }

    private BodyRequest getInterActiveBody() {
        BodyRequest bodyRequest = new BodyRequest();
        bodyRequest.setText("body Text");
        return bodyRequest;
    }

    private HeaderRequest getInterActiveHeader() {
        HeaderRequest headerRequest = new HeaderRequest();
        headerRequest.setType("text");
        headerRequest.setText("header text");
        return headerRequest;
    }


    private WhatsAppMessageRequest getWhatsAppMessageRequest() {
        WhatsAppMessageRequest whatsAppMessageRequest = new WhatsAppMessageRequest();
        whatsAppMessageRequest.setMessagingProduct("whatsapp");
        whatsAppMessageRequest.setTo("9148671766");
        whatsAppMessageRequest.setType("interactive");
        InteractiveRequest interactiveRequest = getInterActiveList();
        whatsAppMessageRequest.setInteractive(interactiveRequest);
        return whatsAppMessageRequest;
    }

}