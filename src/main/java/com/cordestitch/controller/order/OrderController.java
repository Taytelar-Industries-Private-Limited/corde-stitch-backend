package com.cordestitch.controller.order;

import com.cordestitch.request.order.CancelOrderRequest;
import com.cordestitch.request.order.CheckReturnedProductRequest;
import com.cordestitch.request.order.OrderRequest;
import com.cordestitch.request.order.ReturnRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.order.*;
import com.cordestitch.service.service.order.OrderService;
import com.cordestitch.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    /**
     * Places an order based on the provided order request.

     * This API endpoint processes an order request and returns the details
     * of the placed order. It expects a valid OrderRequest object in the request body.
     * If the order is successfully placed, it returns a PlaceAnOrderResponse with
     * the details of the order.
     *
     * @param orderRequest The request object containing order details.
     * @return A ResponseEntity containing the PlaceAnOrderResponse with the status code.
     */
    @PostMapping("/placeAnOrder")
    public ResponseEntity<PlaceAnOrderResponse> placeAnOrder(@Valid @RequestBody OrderRequest orderRequest) {
        PlaceAnOrderResponse placeAnOrderResponse = orderService.placeAnOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.OK).body(placeAnOrderResponse);
    }

    /**
     * Retrieves all orders for a specific user.
     * This API endpoint fetches a list of all orders associated with the user.
     * The userId is extracted from the servlet request attributes to identify the user.
     * The response includes a GetAllOrdersResponse containing details of each order.
     *
     * @param request The HttpServletRequest containing the userId attribute.
     * @return A ResponseEntity containing a GetAllOrdersResponse with the user's order details.
     */
    @GetMapping("/getAllOrders")
    public ResponseEntity<GetAllOrdersResponse> getAllOrders(HttpServletRequest request) {
        String userId = (String) request.getAttribute(Constants.USERID);
        GetAllOrdersResponse getAllOrdersResponse = orderService.getAllOrders(userId);
        return ResponseEntity.status(HttpStatus.OK).body(getAllOrdersResponse);
    }
    /**
     * Retrieves details of a specific order based on the orderId and userId.
     * This API endpoint fetches detailed information for a specific order,
     * ensuring that the userId matches the owner of the order.
     *
     * @param orderId The ID of the order to fetch details for.
     * @param request  userId The ID of the user who placed the order.
     * @return A ResponseEntity containing an OrderSummaryResponse with the order details.
     *         The status of the response is set to OK (200).
     */

    @GetMapping("/getOrderDetailsByOrderId")
    public ResponseEntity<OrderSummaryResponse> getOrderDetailsByOrderId(HttpServletRequest request,@RequestParam String orderId) {
        String userId = (String) request.getAttribute(Constants.USERID);
        OrderSummaryResponse orderSummaryResponse = orderService.getOrderDetailsByOrderId(orderId,userId);
        return ResponseEntity.status(HttpStatus.OK).body(orderSummaryResponse);
    }

    @PostMapping("/cancelOrder")
    public ResponseEntity<CancelOrderResponse> cancelOrder(@Valid @RequestBody CancelOrderRequest cancelOrderRequest) {
        CancelOrderResponse cancelOrderResponse = orderService.cancelOrder(cancelOrderRequest);
        return ResponseEntity.status(HttpStatus.OK).body(cancelOrderResponse);
    }

    @PostMapping("/returnOrder")
    public ResponseEntity<ReturnOrderResponse> returnOrder(@Valid @RequestBody ReturnRequest returnRequest) {
        ReturnOrderResponse response = orderService.returnOrder(returnRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/check-returned-product")
    public ResponseEntity<SuccessResponse> checkReturnedProduct(@Valid @RequestBody CheckReturnedProductRequest request) {
        SuccessResponse successResponse = orderService.checkReturnedProduct(request);
        return ResponseEntity.status(successResponse.getStatusCode()).body(successResponse);
    }

    @DeleteMapping("/revertPlaceAnOrder")
    public ResponseEntity<SuccessResponse> revertPlaceAnOrder(@RequestParam String orderId, HttpServletRequest request) {
        String userId = (String) request.getAttribute(Constants.USERID);
        SuccessResponse successResponse = orderService.revertPlaceAnOrder(orderId, userId);
        return ResponseEntity.status(successResponse.getStatusCode()).body(successResponse);
    }
}
