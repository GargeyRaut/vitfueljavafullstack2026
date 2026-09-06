package com.netsim.api.websocket;

import com.netsim.engine.SimulationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Observer-pattern subscriber: forwards every published
 * {@link SimulationEvent} to the STOMP topic
 * {@code /topic/simulations/{runId}} so connected clients see the run
 * unfold live, independent of the {@code SimulationEventCollector}
 * buffer used for persistence.
 */
@Component
public class SimulationEventSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public SimulationEventSocketHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onEvent(SimulationEvent event) {
        messagingTemplate.convertAndSend("/topic/simulations/" + event.runId(), SimulationEventDTO.from(event));
    }
}
