package com.bandwidth.controller;

import com.bandwidth.Main;
import com.bandwidth.model.CallRequest;
import com.bandwidth.model.VoiceReply;

import com.bandwidth.sdk.ApiClient;
import com.bandwidth.sdk.ApiResponse;
import com.bandwidth.sdk.ApiException;
import com.bandwidth.sdk.auth.HttpBasicAuth;
import com.bandwidth.sdk.Configuration;
import com.bandwidth.sdk.model.*;
import com.bandwidth.sdk.api.CallsApi;
import com.bandwidth.sdk.model.CreateCall;
import com.bandwidth.sdk.model.CreateCallResponse;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("calls")
public class CallController {

    Logger logger = LoggerFactory.getLogger(CallController.class);

    private final String username = System.getenv("BW_USERNAME");
    private final String password = System.getenv("BW_PASSWORD");
    private final String accountId = System.getenv("BW_ACCOUNT_ID");
    private final String applicationId = System.getenv("BW_VOICE_APPLICATION_ID");
    private final String answerUrl = System.getenv("BASE_CALLBACK_URL");

    public ApiClient defaultClient = Configuration.getDefaultApiClient();
    public HttpBasicAuth Basic = (HttpBasicAuth) defaultClient.getAuthentication("Basic");
    public final CallsApi api = new CallsApi(defaultClient);
    private static CreateCall createCallBody = new CreateCall();


    @PostMapping()
    public VoiceReply createCall(@RequestBody CallRequest callRequest) throws ApiException, URISyntaxException {

        Basic.setUsername(username);
        Basic.setPassword(password);
        createCallBody.setTo(callRequest.getTo());
        createCallBody.setFrom(callRequest.getFrom());
        createCallBody.setApplicationId(applicationId);
        createCallBody.setAnswerUrl(new URI(answerUrl + "/callbacks/outbound/voice"));


        VoiceReply voiceReply = new VoiceReply();
        try {
            ApiResponse<CreateCallResponse> response = api.createCallWithHttpInfo(accountId, createCallBody);
            voiceReply.setSuccess(true);
        } catch (ApiException e) { // Bandwidth API response status not 2XX
            voiceReply.setSuccess(false);
            voiceReply.setError(e.getMessage());
        }

        return voiceReply;
    }
}
