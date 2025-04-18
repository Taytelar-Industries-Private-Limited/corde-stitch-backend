package com.cordestitch.controller.loyalty;

import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.loyalty.LoyaltyPointsResponse;
import com.cordestitch.response.loyalty.PointsRedemptionResponse;
import com.cordestitch.service.service.loyalty.LoyaltyPointsService;
import com.cordestitch.service.service.token.JwtService;
import com.cordestitch.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(LoyaltyPointController.class)
class LoyaltyPointControllerTest {
    @MockBean
    public LoyaltyPointsService loyaltyPointsService;

    @MockBean
    public JwtService jwtService;

    @Autowired
    public WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }


    @Test
    void processReferral() throws Exception {
        String referrerCode = "ABC123";
        String userId = "UID123";

        when(loyaltyPointsService.processReferral(referrerCode,userId)).thenReturn(new SuccessResponse(Constants.LOYALTY_POINTS, HttpStatus.OK.value()));
        mockMvc.perform(post("/api/loyalty/processReferral")
                .param("referrerCode",referrerCode)
                        .requestAttr(Constants.USERID,userId))
                .andExpect(status().isOk());
    }

    @Test
    void processOrderItemLoyaltyPoints() throws Exception{
        String orderItemId = "IT123";
        String userId = "UID123";
        when(loyaltyPointsService.processOrderLoyaltyPoints(orderItemId,userId)).thenReturn(new SuccessResponse(Constants.SUCCESS, 200));
        mockMvc.perform(post("/api/loyalty/processOrderItemLoyaltyPoints")
                        .param("orderItemId",orderItemId)
                        .requestAttr(Constants.USERID,userId))
                .andExpect(status().isOk());
    }

    @Test
    void getLoyaltyPointsSummary() throws Exception {
        String userId = "UID123";
        when(loyaltyPointsService.getLoyaltyPointsSummary(userId)).thenReturn(new LoyaltyPointsResponse());
        mockMvc.perform(get("/api/loyalty/getLoyaltyPointsSummary")
                        .requestAttr(Constants.USERID,userId))
                .andExpect(status().isOk());
    }

    @Test
    void test_redeem_money() throws Exception {
        String userId = "UID123";
        when(loyaltyPointsService.redeemMoneyFromPoints(any(), any())).thenReturn(new PointsRedemptionResponse(1000.00, Constants.SUCCESS, 200));
        mockMvc.perform(post("/api/loyalty/redeemMoney")
                .param("totalRedeemablePoints", String.valueOf(1000.00))
                .requestAttr(Constants.USERID, userId))
                .andExpect(status().isOk());
    }

    @Test
    void test_process_loyaltyPoints_to_account_success() throws Exception {
        when(loyaltyPointsService.entityProcessLoyaltyPointsToAccount()).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/loyalty/processLoyaltyPointsToAccount"))
                .andExpect(status().isOk());
    }
}