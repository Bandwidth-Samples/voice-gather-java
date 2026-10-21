package com.bandwidth.controller;

import com.bandwidth.sdk.model.AnswerCallback;
import com.bandwidth.sdk.model.GatherCallback;
import com.bandwidth.sdk.model.bxml.*;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("callbacks")
public class CallbacksController {

    Logger logger = LoggerFactory.getLogger(CallbacksController.class);

    @PostMapping(value = "/outbound/voice", produces = "application/xml")
    public ResponseEntity<String> voiceCallback(@RequestBody AnswerCallback callback) throws JAXBException {
        Response response = new Response();
        JAXBContext jaxbContext = JAXBContext.newInstance(Response.class);

        String eventType = String.valueOf(callback.getEventType());
        switch (eventType) {
            case "answer":
                SpeakSentence speakSentence = new SpeakSentence("Press 1 to choose option 1. Press 2 to choose option 2. Press pound when you are finished.");
                Gather gather = new Gather().builder()
                        .gatherUrl("/callbacks/outbound/voice/gather")
                        .terminatingDigits("#")
                        .children(List.of(speakSentence))
                        .build();

                response.with(gather);
                break;
            case "initiate":
                response.withVerbs(
                        new SpeakSentence("Initiate event received but not intended. Ending call."),
                        new Hangup()
                );
                break;
            case "disconnect":
                logger.info("Call " + callback.getCallId() + " has disconnected");
                break;
            default:
                logger.info("Unexpected event type " + eventType + " received");
                break;
        }

        return ResponseEntity.ok(response.toBxml(jaxbContext));
    }

    @PostMapping(value = "/outbound/voice/gather", produces = "application/xml")
    public ResponseEntity<String> gatherCallback(@RequestBody GatherCallback callback) throws JAXBException {
        if (!"gather".equals(callback.getEventType())) {
            return ResponseEntity.badRequest().build();
        }

        SpeakSentence speakSentence;
        if ("1".equals(callback.getDigits())) {
            speakSentence = new SpeakSentence("You chose option 1. Goodbye.");
        } else if ("2".equals(callback.getDigits())) {
            speakSentence = new SpeakSentence("You chose option 2. Goodbye.");
        } else {
            speakSentence = new SpeakSentence("You did not choose a valid option. Goodbye.");
        }

        Response response = new Response();
        response.with(speakSentence);

        return ResponseEntity.ok(response.toBxml(JAXBContext.newInstance(Response.class)));
    }

}
