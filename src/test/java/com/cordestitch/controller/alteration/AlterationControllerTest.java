package com.cordestitch.controller.alteration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.request.alteration.BookMeasurementSlotRequest;
import com.cordestitch.request.alteration.BookSlotRequest;
import com.cordestitch.request.alteration.GetSlotTimesRequest;
import com.cordestitch.request.alteration.RescheduleOrCancelRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.alteration.AlterationResponse;
import com.cordestitch.response.alteration.BookedSlotResponse;
import com.cordestitch.response.alteration.SlotTimes;
import com.cordestitch.service.service.alteration.AlterationService;
import com.cordestitch.service.serviceimplementation.token.JwtServiceImplementation;
import com.cordestitch.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AlterationController.class)
@AutoConfigureMockMvc
class AlterationControllerTest {

    @MockBean
    public AlterationService alterationService;

    @Autowired
    public WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    public IdEncryptor idEncryptor;

    @MockBean
    public JwtServiceImplementation jwtServiceImplementation;

    @Autowired
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ENCRYPTED_USER_ID = "dGsWBYqcp6YJfgrJEgShJ49SzBm7Uv6FTR9aKPvdJOGeuUC7Ow";
    private static final String DECRYPTED_USER_ID = "user1";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        when(idEncryptor.decrypt(ENCRYPTED_USER_ID))
                .thenReturn(DECRYPTED_USER_ID);
    }

    @Test
    void testGetOrderItems() throws Exception {
        String phoneNumber = "1234567890";
        String emailAddress = "user@gmail.com";
        when(alterationService.getOrderItems(any(), any(), any())).thenReturn(new AlterationResponse());
        mockMvc.perform(get("/api/alteration/getOrderItems")
                        .param("userId", ENCRYPTED_USER_ID)
                        .param("phoneNumber", phoneNumber)
                        .param("emailAddress", emailAddress)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetSlotTimes() throws Exception {
        GetSlotTimesRequest request = getSlotTimeRequest();
        when(alterationService.getSlotTimes(any())).thenReturn(new SlotTimes());
        mockMvc.perform(post("/api/alteration/getSlotTimes")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testBookSlotTimes() throws Exception {
        BookSlotRequest request = getBookSlotRequest();
        when(alterationService.bookSlotTimes(any())).thenReturn(new SuccessResponse(Constants.SLOT_BOOKED_SUCCESSFULLY, HttpStatus.OK.value()));
        mockMvc.perform(post("/api/alteration/bookSlotTimes")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetBookedSlotTimes() throws Exception {
        when(alterationService.getBookedSlotTimes(any())).thenReturn(new BookedSlotResponse());
        mockMvc.perform(get("/api/alteration/getBookedSlotTimes")
                        .param("userId", ENCRYPTED_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testRescheduleOrCancelSlotTimes() throws Exception {
        RescheduleOrCancelRequest request = getRescheduleOrCancelRequest();
        request.setReschedule(true);
        when(alterationService.rescheduleOrCancelSlotTimes(any())).thenReturn(new SuccessResponse(Constants.SLOT_TIME_RESCHEDULED_SUCCESSFULLY, HttpStatus.OK.value()));
        mockMvc.perform(post("/api/alteration/rescheduleOrCancelSlotTimes")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    void testRescheduleOrCancelSlotTimes_When_Request_Reschedule_Is_False() throws Exception {
        RescheduleOrCancelRequest request = getRescheduleOrCancelRequest();
        when(alterationService.rescheduleOrCancelSlotTimes(any())).thenReturn(new SuccessResponse());
        mockMvc.perform(post("/api/alteration/rescheduleOrCancelSlotTimes")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRescheduleOrCancelSlotTimes_When_Request_Reschedule_Is_False_And_NewSlotDate_Start_And_EndTime_Is_Null() throws Exception {
        RescheduleOrCancelRequest request = getRescheduleOrCancelRequest();
        request.setNewEndTime(null);
        request.setNewStartTime(null);
        request.setNewSlotDate(null);
        when(alterationService.rescheduleOrCancelSlotTimes(any())).thenReturn(new SuccessResponse(Constants.CANCELLED_SLOT_TIME_SUCCESSFULLY, HttpStatus.OK.value()));
        mockMvc.perform(post("/api/alteration/rescheduleOrCancelSlotTimes")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testRescheduleOrCancelSlotTimes_When_Request_Reschedule_Is_True() throws Exception {
        RescheduleOrCancelRequest request = getRescheduleOrCancelRequest();
        request.setReschedule(true);
        request.setNewEndTime(null);
        request.setNewStartTime(null);
        request.setNewSlotDate(null);
        when(alterationService.rescheduleOrCancelSlotTimes(any())).thenReturn(new SuccessResponse());
        mockMvc.perform(post("/api/alteration/rescheduleOrCancelSlotTimes")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRescheduleOrCancelSlotTimes_When_Request_Is_Null() throws Exception {
        RescheduleOrCancelRequest request = new RescheduleOrCancelRequest();
        when(alterationService.rescheduleOrCancelSlotTimes(any())).thenReturn(new SuccessResponse());
        mockMvc.perform(post("/api/alteration/rescheduleOrCancelSlotTimes")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testBookMeasurementSlot() throws Exception {
        BookMeasurementSlotRequest request = getBookMeasurementSlotRequest();
        when(alterationService.bookMeasurementSlot(any())).thenReturn(new SuccessResponse(Constants.SLOT_BOOKED_SUCCESSFULLY, HttpStatus.OK.value()));
        mockMvc.perform(post("/api/alteration/bookMeasurementSlot")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private BookMeasurementSlotRequest getBookMeasurementSlotRequest() {
        BookMeasurementSlotRequest request = new BookMeasurementSlotRequest();
        request.setAddressId("address1");
        request.setUserId(ENCRYPTED_USER_ID);
        request.setSlotDate(LocalDate.now().plusDays(1));
        request.setEndTime(LocalTime.now().plusHours(5));
        request.setStartTime(LocalTime.now());
        return request;
    }

    private RescheduleOrCancelRequest getRescheduleOrCancelRequest() {
        RescheduleOrCancelRequest request = new RescheduleOrCancelRequest();
        request.setReschedule(false);
        request.setEndTime(LocalTime.now().plusHours(4));
        request.setStartTime(LocalTime.now());
        request.setUserId(ENCRYPTED_USER_ID);
        request.setSlotDate(LocalDate.now());
        request.setNewEndTime(LocalTime.now().plusHours(6));
        request.setNewStartTime(LocalTime.now().plusHours(1));
        request.setNewSlotDate(LocalDate.now().plusDays(1));
        return request;
    }

    private BookSlotRequest getBookSlotRequest() {
        BookSlotRequest request = new BookSlotRequest();
        request.setUserId(ENCRYPTED_USER_ID);
        request.setStartTime(LocalTime.now().minusHours(5));
        request.setEndTime(LocalTime.now());
        request.setSlotDate(LocalDate.now());
        request.setOrderItemIds(List.of("order1", "order2"));
        return request;
    }

    private GetSlotTimesRequest getSlotTimeRequest() {
        GetSlotTimesRequest request = new GetSlotTimesRequest();
        request.setSlotDate(LocalDate.now());
        request.setOrderItemIds(List.of("order1", "order2"));
        return request;
    }
}