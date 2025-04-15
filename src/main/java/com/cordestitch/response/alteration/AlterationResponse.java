package com.cordestitch.response.alteration;

import com.cordestitch.response.order.OrderItemResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlterationResponse {

    List<OrderItemResponse> orderItemResponses = new ArrayList<>();
}