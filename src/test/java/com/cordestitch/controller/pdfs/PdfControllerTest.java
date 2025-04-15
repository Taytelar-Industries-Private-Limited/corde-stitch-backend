package com.cordestitch.controller.pdfs;

import com.cordestitch.response.SuccessResponse;
import com.cordestitch.service.service.pdfs.*;
import com.cordestitch.service.serviceimplementation.token.JwtServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PdfController.class)
class PdfControllerTest {

    @Autowired
    public WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    public JwtServiceImplementation jwtServiceImplementation;

    @MockBean
    public OrderCancelledService orderCancelledService;

    @MockBean
    private ReturnOrderService returnOrderService;

    @MockBean
    private StockPdfGeneratorService stockPdfGeneratorService;

    @MockBean
    private PdfGenerateService pdfGenerateService;

    @MockBean
    private DeliveredOrderService deliveredOrderService;

    @MockBean
    private OrderConfirmedService orderConfirmedService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void getDailyReport() throws Exception{
        int reportType = 1;
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setStatusCode(200);

        when(pdfGenerateService.sendReportToEmail(reportType)).thenReturn(successResponse);
        mockMvc.perform(get("/api/pdf/report")
                        .param("reportType", String.valueOf(reportType))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getDeliveredOrderReport() throws Exception {
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setStatusCode(200);
        when(deliveredOrderService.sendDeliveredOrderReportToEmail()).thenReturn(successResponse);
        mockMvc.perform(get("/api/pdf/deliveredOrderReport")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getProductReport() throws Exception{
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setStatusCode(200);
        when(stockPdfGeneratorService.sendStockPdfReportToEmail()).thenReturn(successResponse);
        mockMvc.perform(get("/api/pdf/productGetReport")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getConfirmedOrdersReport() throws Exception{
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setStatusCode(200);
        when(orderConfirmedService.sendOrderConfirmedEmail()).thenReturn(successResponse);
        mockMvc.perform(get("/api/pdf/confirmedOrdersReport")
                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
    }

    @Test
    void getCancelledOrdersReport() throws Exception{
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setStatusCode(200);
        when(orderCancelledService.sendOrderCancelledEmail()).thenReturn(successResponse);
        mockMvc.perform(get("/api/pdf/cancelledOrdersReport")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getReturnedOrdersReport() throws Exception{
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setStatusCode(200);
        when(returnOrderService.sendReturnOrderEmail()).thenReturn(successResponse);
        mockMvc.perform(get("/api/pdf/returnOrderReport")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}