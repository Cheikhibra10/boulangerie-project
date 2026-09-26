package com.boulangerie.notifications;

import com.boulangerie.notifications.dto.NotificationDto;
import com.boulangerie.notifications.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationBroadcaster {

    public static final String TOPIC = "/topic/notifications";

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(Notification notification) {
        messagingTemplate.convertAndSend(TOPIC, NotificationDto.from(notification));
    }
}