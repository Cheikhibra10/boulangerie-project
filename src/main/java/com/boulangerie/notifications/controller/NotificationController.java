package com.boulangerie.notifications.controller;

import com.boulangerie.notifications.NotificationStore;
import com.boulangerie.notifications.dto.NotificationDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Gestion notifications")
public class NotificationController {

    private final NotificationStore store;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> findAll(
            @RequestParam(name = "unreadOnly", defaultValue = "false") boolean unreadOnly
    ) {
        List<NotificationDto> notifications = store.findAll(unreadOnly).stream()
                .map(NotificationDto::from)
                .toList();

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        return ResponseEntity.ok(Map.of("count", store.countUnread()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markAsRead(@PathVariable String id) {
        return store.markAsRead(id)
                .map(NotificationDto::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        store.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clear() {
        store.clear();
        return ResponseEntity.noContent().build();
    }
}