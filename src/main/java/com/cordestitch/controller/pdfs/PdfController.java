package com.cordestitch.controller.pdfs;

import com.cordestitch.response.SuccessResponse;
import com.cordestitch.service.service.pdfs.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pdf")
public class PdfController {

    private final StockPdfGeneratorService stockPdfGeneratorService;
    private final PdfGenerateService pdfGenerateService;
    private final DeliveredOrderService deliveredOrderService;
    private final OrderConfirmedService orderConfirmedService;
    private final OrderCancelledService orderCancelledService;
    private final ReturnOrderService returnOrderService;

    @GetMapping("/report")
    public ResponseEntity<SuccessResponse> getDailyReport(@RequestParam int reportType) {
        SuccessResponse response = pdfGenerateService.sendReportToEmail(reportType);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/deliveredOrderReport")
    public ResponseEntity<SuccessResponse> getDeliveredOrderReport() {
        SuccessResponse response = deliveredOrderService.sendDeliveredOrderReportToEmail();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/productGetReport")
    public ResponseEntity<SuccessResponse> getProductReport() {
        SuccessResponse response = stockPdfGeneratorService.sendStockPdfReportToEmail();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/confirmedOrdersReport")
    public ResponseEntity<SuccessResponse> getConfirmedOrdersReport() {
        SuccessResponse response = orderConfirmedService.sendOrderConfirmedEmail();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/cancelledOrdersReport")
    public ResponseEntity<SuccessResponse> getCancelledOrdersReport() {
        SuccessResponse response = orderCancelledService.sendOrderCancelledEmail();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    @GetMapping("/returnOrderReport")
    public ResponseEntity<SuccessResponse> getReturnedOrdersReport() {
        SuccessResponse response = returnOrderService.sendReturnOrderEmail();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
