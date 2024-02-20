package com.bandwidth.controller;

import com.bandwidth.sdk.model.bxml.*;
import com.bandwidth.sdk.model.bxml.Response;
import com.bandwidth.sdk.model.AnswerCallback;
import com.bandwidth.sdk.model.GatherCallback;

import java.util.List;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("callbacks")
public class CallbacksController {

    Logger logger = LoggerFactory.getLogger(CallbacksController.class);

    @RequestMapping("/outbound/voice")
    public String voiceCallback(@RequestBody AnswerCallback callback) throws JAXBException {

        Response response = new Response();
	JAXBContext jaxbContext = JAXBContext.newInstance(Response.class);

        logger.info(callback.getEventType());
        logger.info(callback.getCallId());
        switch( callback.getEventType()) {
            case "answer":


		SpeakSentence speakSentence = new SpeakSentence("Please hit the number 1, 2, or 3 followed by pound when finished");
                Gather gather = new Gather().builder()
                        .repeatCount(3)
                        .gatherUrl("/callbacks/outbound/gather")
                        .terminatingDigits("#")
		        .children(List.of(speakSentence))
                        .build();

                response.with(gather);

                return response.toBxml(jaxbContext);
            case "initiate":
                logger.info("The Initiate event is fired when an inbound call is received for an application. Sent to the URL specified in the application.");

                return response.with(
                        new SpeakSentence("Initiate event recieved but not inteded.  Ending call")
		).toBxml(jaxbContext);


            case "disconnect":
                logger.info("The Disconnect event is fired when a call ends, for any reason. The cause for a disconnect event on a call can be:");
                logger.info("Call " + callback.getCallId() + " has disconnected");


                return response.with(
                        new SpeakSentence("Disconnect event recieved but not inteded.  Ending call")
                ).toBxml(jaxbContext);

            default:
                return response.with(
                        new SpeakSentence( callback.getEventType() + " event recieved but not inteded.  Ending call")
                ).toBxml(jaxbContext);
        }

    }

    @RequestMapping("/outbound/gather")
    public String gatherCallback(@RequestBody GatherCallback callback) throws JAXBException {

        Response response = new Response();
	JAXBContext jaxbContext = JAXBContext.newInstance(Response.class);

        logger.info(callback.getEventType());
        logger.info(callback.getCallId());
        if("gather".equalsIgnoreCase(callback.getEventType())) {

            String digit = callback.getDigits();

            SpeakSentence speakSentence;
            if(digit.equalsIgnoreCase("1")) {
                speakSentence = new SpeakSentence("You have chosen choice 1");
            } else if(digit.equalsIgnoreCase("2")) {
                speakSentence = new SpeakSentence("You have chosen choice 2");
            } else {
                speakSentence = new SpeakSentence("Invalid choice");
            }

           response.with(speakSentence);
        }
        return response.toBxml(jaxbContext);
    }

}
