package com.smartmoneymanager.backend.mapper;

import org.springframework.stereotype.Component;

import com.smartmoneymanager.backend.dto.response.NotificationResponse;
import com.smartmoneymanager.backend.entity.Notification;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
