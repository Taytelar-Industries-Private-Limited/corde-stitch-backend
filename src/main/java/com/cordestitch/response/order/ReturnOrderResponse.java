package com.cordestitch.response.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnOrderResponse {

    private OrderExchangeResponse orderExchangeResponse;

    private OrderRefundResponse orderRefundResponse;
}
