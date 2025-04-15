package com.cordestitch.service.service.order;

import com.cordestitch.request.order.CancelOrderRequest;
import com.cordestitch.request.order.CheckReturnedProductRequest;
import com.cordestitch.request.order.OrderRequest;
import com.cordestitch.request.order.ReturnRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.order.*;


public interface OrderService {
    PlaceAnOrderResponse placeAnOrder(OrderRequest orderRequest);

    CancelOrderResponse cancelOrder(CancelOrderRequest cancelOrderRequest);

    ReturnOrderResponse returnOrder(ReturnRequest returnRequest);

    GetAllOrdersResponse getAllOrders(String userId);

    OrderSummaryResponse getOrderDetailsByOrderId(String orderId,String userId);

    SuccessResponse checkReturnedProduct(CheckReturnedProductRequest request);

    SuccessResponse revertPlaceAnOrder(String orderId, String userId);
}
