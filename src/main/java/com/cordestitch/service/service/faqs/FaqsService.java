package com.cordestitch.service.service.faqs;

import com.cordestitch.request.faqs.FaqsRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.faqs.FaqsResponse;

import java.util.List;

public interface FaqsService {
    SuccessResponse addFaqs(FaqsRequest faqsRequest);

    List<FaqsResponse> getAllFaqs();

    SuccessResponse updateFaqs(FaqsRequest faqsRequest);

    SuccessResponse deleteFaqs(FaqsRequest faqsRequest);
}
