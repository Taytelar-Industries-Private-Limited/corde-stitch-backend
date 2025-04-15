package com.cordestitch.response.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAllOrdersResponse {

    private List<GetOrdersResponse> recentOrders;

    private List<GetOrdersResponse> buyAgain;

    private List<GetOrdersResponse> cancelledOrders;
}
