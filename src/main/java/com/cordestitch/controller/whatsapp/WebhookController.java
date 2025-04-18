package com.cordestitch.controller.whatsapp;

import com.cordestitch.request.webhook.WebHookEventRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.service.service.webhook.WebHookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webhook")
public class WebhookController {

    private final WebHookService webHookService;


    /**
     * Verifies the webhook for WhatsApp integration.
     * This API endpoint is used to verify the webhook when configuring it with WhatsApp.
     * It takes three parameters: mode, verify token, and challenge.
     * If the verification is successful, it returns the challenge value as a response.
     *
     * @param token The verification token configured in the system.
     * @return The challenge string if the verification is successful.
     */
    @GetMapping
    public String verifyWebhook(@RequestParam("hub.mode") String mode, @RequestParam("hub.verify_token") String token, @RequestParam("hub.challenge") String challenge) {
        return webHookService.verifyWebhook(mode, token, challenge);
    }

    /**
     * Receives and processes incoming WhatsApp webhook events.
     * This API endpoint listens for messages and events sent by WhatsApp Webhooks.
     * It takes a WebHookEventRequest object containing event details.
     * If the event is successfully processed, it returns a SuccessResponse confirming the action.
     *
     * @param eventRequest The request object containing webhook event details.
     * @return A ResponseEntity containing a SuccessResponse confirming the event was processed.
     */
    @PostMapping
    public ResponseEntity<SuccessResponse> receiveMessage(@RequestBody WebHookEventRequest eventRequest) {
        SuccessResponse successResponse = webHookService.handleWebhookEvent(eventRequest);
        return ResponseEntity.status(successResponse.getStatusCode()).body(successResponse);
    }
}