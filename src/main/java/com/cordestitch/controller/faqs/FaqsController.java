package com.cordestitch.controller.faqs;

import com.cordestitch.request.faqs.FaqsRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.faqs.FaqsResponse;
import com.cordestitch.service.service.faqs.FaqsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
public class FaqsController {

    private final FaqsService faqsService;

    @PostMapping("/add-faqs")
    public ResponseEntity<SuccessResponse> addFaqs(@RequestBody FaqsRequest faqsRequest) {
        SuccessResponse response = faqsService.addFaqs(faqsRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-all-faqs")
    public ResponseEntity<List<FaqsResponse>> getAllFaqs() {
        List<FaqsResponse> faqsResponseList = faqsService.getAllFaqs();
        return ResponseEntity.status(HttpStatus.OK).body(faqsResponseList);
    }

    @PutMapping("/update-faqs")
    public ResponseEntity<SuccessResponse> updateFaqs(@RequestBody FaqsRequest faqsRequest) {
        SuccessResponse response = faqsService.updateFaqs(faqsRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("delete-faqs")
    public ResponseEntity<SuccessResponse> deleteFaqs(@RequestBody FaqsRequest faqsRequest) {
        SuccessResponse response = faqsService.deleteFaqs(faqsRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
