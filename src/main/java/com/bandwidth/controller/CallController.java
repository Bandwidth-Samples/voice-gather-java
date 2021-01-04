package com.bandwidth.controller;

import com.bandwidth.BandwidthClient;
import com.bandwidth.Environment;
import com.bandwidth.Model.CreateCall;
import com.bandwidth.Model.VoiceReply;
import com.bandwidth.exceptions.ApiException;
import com.bandwidth.http.response.ApiResponse;
import com.bandwidth.voice.controllers.APIController;
import com.bandwidth.voice.models.ApiCallResponse;
import com.bandwidth.voice.models.ApiCreateCallRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("calls")
public class CallController {

    Logger logger = LoggerFactory.getLogger(CallController.class);

    private final String username = System.getenv("BANDWIDTH_USERNAME");
    private final String password = System.getenv("BANDWIDTH_PASSWORD");
    private final String accountId = System.getenv("BANDWIDTH_ACCOUNT_ID");
    private final String applicationId = System.getenv("BANDWIDTH_VOICE_APPLICATION_ID");

    private final BandwidthClient client = new BandwidthClient.Builder()
            .voiceBasicAuthCredentials(username, password)
            .environment(Environment.PRODUCTION)
            .build();

    private final APIController controller = client.getVoiceClient().getAPIController();

    @PostMapping()
    public VoiceReply createCall(@RequestBody CreateCall createCall) throws IOException {

        // Build the body of the call request to the Bandwidth API
        ApiCreateCallRequest callRequest = new ApiCreateCallRequest.Builder()
                .answerUrl("http://32e2578009f7.ngrok.io/callbacks/voiceCallback")
                .applicationId(applicationId)
                .to(createCall.getTo())
                .from(createCall.getFrom())
                .build();

        VoiceReply voiceReply = new VoiceReply();
        try {
            ApiResponse<ApiCallResponse> response = controller.createCall(accountId, callRequest);
            voiceReply.setSuccess(true);
        } catch (ApiException e) { // Bandwidth API response status not 2XX
            voiceReply.setSuccess(false);
            voiceReply.setError(e.getMessage());
        }

        return voiceReply;
    }
}
