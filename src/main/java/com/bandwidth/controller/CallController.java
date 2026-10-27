package com.bandwidth.controller;

import com.bandwidth.model.CallRequest;

import com.bandwidth.sdk.ApiClient;
import com.bandwidth.sdk.api.CallsApi;
import com.bandwidth.sdk.model.CreateCall;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("calls")
public class CallController {

    Logger logger = LoggerFactory.getLogger(CallController.class);

    private final String accountId = System.getenv("BW_ACCOUNT_ID");
    private final String clientId = System.getenv("BW_CLIENT_ID");
    private final String clientSecret = System.getenv("BW_CLIENT_SECRET");
    private final String applicationId = System.getenv("BW_VOICE_APPLICATION_ID");
    private final String number = System.getenv("BW_NUMBER");
    private final String baseCallbackUrl = System.getenv("BASE_CALLBACK_URL");

    private final CallsApi callsApi = new CallsApi(new ApiClient(clientId, clientSecret, null));

    @PostMapping()
    public ResponseEntity<Void> createCall(@RequestBody CallRequest callRequest) {
        if (callRequest.getTo() == null) {
            return ResponseEntity.badRequest().build();
        }

        CreateCall createCall = new CreateCall()
                .applicationId(applicationId)
                .to(callRequest.getTo())
                .from(number)
                .answerUrl(URI.create(baseCallbackUrl + "/callbacks/outbound/voice"));

        try {
            callsApi.createCall(accountId, createCall);
        } catch (Exception e) {
            logger.error("Failed to create call: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.noContent().build();
    }
}
