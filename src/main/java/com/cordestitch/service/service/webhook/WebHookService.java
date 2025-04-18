package com.cordestitch.service.service.webhook;

import com.cordestitch.request.webhook.WebHookEventRequest;
import com.cordestitch.response.SuccessResponse;

public interface WebHookService {

    String verifyWebhook(String mode, String token, String challenge);

    SuccessResponse handleWebhookEvent(WebHookEventRequest eventRequest);
}