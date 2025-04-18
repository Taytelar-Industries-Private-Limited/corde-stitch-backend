package com.cordestitch.response.faqs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FaqsResponse {

    private String faqsId;

    private String question;

    private String answer;
}
