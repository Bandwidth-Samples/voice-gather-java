package com.bandwidth.controller;

import com.bandwidth.BandwidthClient;
import com.bandwidth.Environment;
import com.bandwidth.Model.VoiceCallback;
import com.bandwidth.voice.bxml.verbs.Gather;
import com.bandwidth.voice.bxml.verbs.Response;
import com.bandwidth.voice.bxml.verbs.SpeakSentence;
import com.bandwidth.voice.controllers.APIController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;

@RestController
@RequestMapping("callbacks")
public class CallbacksController {

    Logger logger = LoggerFactory.getLogger(CallbacksController.class);

    private String username = System.getenv("BANDWIDTH_API_USERNAME");
    private String password = System.getenv("BANDWIDTH_API_PASSWORD");
    private String accountId = System.getenv("BANDWIDTH_ACCOUNT_ID");
    private String applicationId = System.getenv("BANDWIDTH_VOICE_APPLICATION_ID");

    private BandwidthClient client = new BandwidthClient.Builder()
            .voiceBasicAuthCredentials(username, password)
            .environment(Environment.PRODUCTION)
            .build();

    private APIController controller = client.getVoiceClient().getAPIController();



    @RequestMapping("/voiceCallback")
    public String voiceCallback(@RequestBody VoiceCallback callback) throws IOException, MalformedURLException {

        Response response = new Response();

        logger.info(callback.getEventType());
        logger.info(callback.getCallId());
        switch( callback.getEventType()) {
            case "answer":

                Gather gather = Gather.builder()
                        .audioProducer(
                                SpeakSentence.builder()
                                        .text("Text goes here hit pound when finished")
                                        .build()
                        )
                        .repeatCount(3)
                        .gatherUrl("/callbacks/gatherCallback")
                        .terminatingDigits("#")
                        .build();

                response.add(gather);

                return response.toBXML();
            case "initiate":
                logger.info("The Initiate event is fired when an inbound call is received for an application. Sent to the URL specified in the application.");

                return response.add(
                        SpeakSentence.builder().text("Initiate event recieved but not inteded.  Ending call").build()
                ).toBXML();

            case "disconnect":
                logger.info("The Disconnect event is fired when a call ends, for any reason. The cause for a disconnect event on a call can be:");
                logger.info("Call " + callback.getCallId() + " has disconnected");

                return response.add(
                        SpeakSentence.builder().text("Disconnect event recieved but not inteded.  Ending call").build()
                ).toBXML();

            default:
                return response.add(
                        SpeakSentence.builder().text( callback.getEventType() + " event recieved but not inteded.  Ending call").build()
                ).toBXML();
        }

    }

    @RequestMapping("/gatherCallback")
    public String gatherCallback(@RequestBody VoiceCallback callback) throws IOException, MalformedURLException  {

        Response response = new Response();

        logger.info(callback.getEventType());
        logger.info(callback.getCallId());
        switch( callback.getEventType()) {
            case "gather":

                String digit = callback.getDigits();

                SpeakSentence speakSentence;
                if(digit.equalsIgnoreCase("1")) {
                    speakSentence = SpeakSentence.builder().text("You have chosen choice 1").build();
                } else if(digit.equalsIgnoreCase("2")) {
                    speakSentence = SpeakSentence.builder().text("You have chosen choice 2").build();
                } else {
                    speakSentence = SpeakSentence.builder().text("Invalid choice").build();
                }

                return response.add(speakSentence).toBXML();
            default:
                break;
        }
        return response.toBXML();
    }

}