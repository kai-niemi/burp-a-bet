package io.burpabet.betting.service;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class Pusher {
    public static final String TOPIC_BET_SETTLEMENT = "/topic/bet-settlement";

    public static final String TOPIC_BET_PLACEMENT = "/topic/bet-placement";

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    public void convertAndSend(String topic, Object payload, int delaySeconds) {
        scheduledExecutorService.schedule(() ->
                simpMessagingTemplate.convertAndSend(topic, payload), delaySeconds, TimeUnit.SECONDS);
    }
}
