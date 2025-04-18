package com.cordestitch.controller.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.enums.OrderStatus;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.request.order.CancelOrderRequest;
import com.cordestitch.request.order.OrderItemRequest;
import com.cordestitch.request.order.OrderRequest;
import com.cordestitch.request.order.ReturnRequest;
import com.cordestitch.request.user.AddressRequest;
import com.cordestitch.response.order.*;
import com.cordestitch.service.service.order.OrderService;
import com.cordestitch.service.serviceimplementation.token.JwtServiceImplementation;
import com.cordestitch.util.Constants;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @MockBean
    public OrderService orderService;
    @Autowired
    public WebApplicationContext webApplicationContext;
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    public IdEncryptor idEncryptor;

    @MockBean
    public JwtServiceImplementation jwtServiceImplementation;
    private final ObjectMapper mapper = new ObjectMapper();

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
    void testPlaceAnOrder() throws Exception {
        OrderRequest orderRequest = getOrderRequest();
        when(orderService.placeAnOrder(any())).thenReturn(getSuccessResponse());
        mockMvc.perform(post("/api/order/placeAnOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(orderRequest)))
                .andExpect(status().isOk());

    }

    @Test
    void testGetAllOrders() throws Exception {
        String userId = ENCRYPTED_USER_ID;
        when(orderService.getAllOrders(userId)).thenReturn(new GetAllOrdersResponse());
        mockMvc.perform(get("/api/order/getAllOrders")
                        .param("userId",userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void testCancelAnOrder() throws Exception {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        when(orderService.cancelOrder(any())).thenReturn(getCancelOrderResponse());
        mockMvc.perform(post("/api/order/cancelOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(cancelOrderRequest)))
                .andExpect(status().isOk());

    }

    @Test
    void testReturnOrder() throws Exception {
        ReturnRequest returnRequest = getReturnRequest();
        when(orderService.returnOrder(any())).thenReturn(getReturnResponse());
        mockMvc.perform(post("/api/order/returnOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(returnRequest)))
                .andExpect(status().isOk());

    }

    @Test
    void testGetOrderDetailsByOrderId() throws Exception {
        when(orderService.getOrderDetailsByOrderId(any(), any())).thenReturn(new OrderSummaryResponse());
        mockMvc.perform(get("/api/order/getOrderDetailsByOrderId")
                .param("userId", DECRYPTED_USER_ID)
                .param("orderId", "Order123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private ReturnOrderResponse getReturnResponse() {
        ReturnOrderResponse response = new ReturnOrderResponse();
        response.setOrderExchangeResponse(getOrderExchangeResponse());
        response.setOrderRefundResponse(getOrderRefundResponse());
        return response;
    }

    private OrderRefundResponse getOrderRefundResponse() {
        OrderRefundResponse orderRefundResponse = new OrderRefundResponse();
        orderRefundResponse.setRefundAmount(10.0);
        orderRefundResponse.setMessage(Constants.REFUND_SUCCESSFUL);
        return orderRefundResponse;
    }

    private OrderExchangeResponse getOrderExchangeResponse() {
        OrderExchangeResponse orderExchangeResponse = new OrderExchangeResponse();
        orderExchangeResponse.setAdditionalAmountRequired(true);
        orderExchangeResponse.setAdditionalAmount(100.00);
        orderExchangeResponse.setOrderId("TT123456");
        orderExchangeResponse.setMessage(Constants.EXCHANGE_SUCCESSFUL);
        return orderExchangeResponse;
    }

    private ReturnRequest getReturnRequest() {
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setOrderId("123456");
        returnRequest.setUserId("123");
        returnRequest.setOrderItemId("123");
        returnRequest.setReason("Not Worth It");
        returnRequest.setReturnType("refund");
        returnRequest.setReplacementOrderItemRequest(getReplacementOrderItemRequest());
        return returnRequest;
    }

    private OrderItemRequest getReplacementOrderItemRequest() {
        OrderItemRequest orderItemRequest = new OrderItemRequest();
        orderItemRequest.setQuantity(1);
        orderItemRequest.setProductId("ID1");
        orderItemRequest.setTotalAmount(100.0);
        orderItemRequest.setColor("white");
        orderItemRequest.setSize(30);
        return orderItemRequest;
    }
    private CancelOrderResponse getCancelOrderResponse() {
        CancelOrderResponse cancelOrderResponse = new CancelOrderResponse();
        cancelOrderResponse.setMessage("Order cancelled successfully");
        cancelOrderResponse.setOrderStatus(OrderStatus.CANCELED);
        cancelOrderResponse.setOrderId("TT123456");
        cancelOrderResponse.setCancelledDate(LocalDateTime.now());
        cancelOrderResponse.setOrderDate(LocalDateTime.now().minusDays(5));
        cancelOrderResponse.setRefundMessage(Constants.REFUND_REQUEST_INITIATED);
        return cancelOrderResponse;
    }

    private CancelOrderRequest getCancelOrderRequest() {
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId("TT123456");
        cancelOrderRequest.setOrderItemId("IT16412709");
        cancelOrderRequest.setUserId(ENCRYPTED_USER_ID);
        cancelOrderRequest.setReason("refund");
        cancelOrderRequest.setIsRefund(true);
        return cancelOrderRequest;
    }

    private PlaceAnOrderResponse getSuccessResponse() {
        PlaceAnOrderResponse placeAnOrderResponse = new PlaceAnOrderResponse();
        placeAnOrderResponse.setMessage("Order placed successfully");
        placeAnOrderResponse.setOrderId("TT123456");
        return placeAnOrderResponse;
    }

    private OrderRequest getOrderRequest() {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(ENCRYPTED_USER_ID);
        orderRequest.setTotalAmount(200.00);
        orderRequest.setOrderItemRequests(getOrderItemRequests());
        orderRequest.setPaymentMethod(Constants.COD);
        orderRequest.setShippingAddress(getAddressRequest());
        return orderRequest;
    }

    private AddressRequest getAddressRequest() {
        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setAddressId("10");
        addressRequest.setLandMark("near Government high school");
        addressRequest.setTypeOfAddress("home");
        addressRequest.setPinCode("560064");
        addressRequest.setStreetName("kogilu");
        addressRequest.setStateName("karnataka");
        addressRequest.setUserId("UID01");
        addressRequest.setBuildingName("ramanashree");
        addressRequest.setCityName("yelahanka");
        addressRequest.setCountryName("India");
        addressRequest.setFirstName("jay");
        addressRequest.setLastName("prakash");
        addressRequest.setPhoneNumber("1234567890");
        return addressRequest;
    }

    private List<OrderItemRequest> getOrderItemRequests() {
        List<OrderItemRequest> requests = new ArrayList<>();

        OrderItemRequest orderItemRequest = new OrderItemRequest();
        orderItemRequest.setProductId("PRODUCT-ID01");
        orderItemRequest.setQuantity(3);
        orderItemRequest.setColor("Blue");
        orderItemRequest.setSize(32);
        orderItemRequest.setTotalAmount(300.00);
        orderItemRequest.setOfferPercentage(20.00);
        requests.add(orderItemRequest);

        return requests;
    }

}