package com.cordestitch.controller.faqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.request.faqs.FaqsRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.faqs.FaqsResponse;
import com.cordestitch.service.service.faqs.FaqsService;
import com.cordestitch.service.service.token.JwtService;
import com.cordestitch.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(FaqsController.class)
class FaqsControllerTest {
    @MockBean
    private FaqsService faqsService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    public WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }
    @Test
    void addFaqs() throws Exception{
        FaqsRequest request = getFaqsRequest();
        SuccessResponse response = new SuccessResponse(Constants.FAQS_ADDED_SUCCESSFULLY,HttpStatus.OK.value());
        when(faqsService.addFaqs(any(FaqsRequest.class))).thenReturn(response);
        mockMvc.perform(post("/api/faqs/add-faqs")
                        .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getAllFaqs() throws Exception {
        List<FaqsResponse> response = new ArrayList<>();
        when(faqsService.getAllFaqs()).thenReturn(response);
        mockMvc.perform(get("/api/faqs/get-all-faqs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateFaqs() throws Exception{
        FaqsRequest request = getFaqsRequest();
        SuccessResponse response = new SuccessResponse(Constants.FAQS_UPDATED_SUCCESSFULLY,HttpStatus.OK.value());
        when(faqsService.updateFaqs(any(FaqsRequest.class))).thenReturn(response);
        mockMvc.perform(put("/api/faqs/update-faqs")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteFaqs() throws Exception{
        FaqsRequest request = getFaqsRequest();
        SuccessResponse response = new SuccessResponse(Constants.FAQS_DELETED_SUCCESSFULLY,HttpStatus.OK.value());
        when(faqsService.deleteFaqs(any(FaqsRequest.class))).thenReturn(response);
        mockMvc.perform(delete("/api/faqs/delete-faqs")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private FaqsRequest getFaqsRequest() {
        FaqsRequest faqsRequest = new FaqsRequest();
        faqsRequest.setFaqsId("UID123");
        faqsRequest.setQuestion("What is the best pant?");
        faqsRequest.setAnswer("It depends on your preferences.");
        return faqsRequest;
    }
}