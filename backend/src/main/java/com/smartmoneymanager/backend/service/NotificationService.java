package com.smartmoneymanager.backend.service;

import java.util.List;

import com.smartmoneymanager.backend.dto.response.NotificationResponse;
import com.smartmoneymanager.backend.entity.enums.NotificationType;

public interface NotificationService {

    List<NotificationResponse> getNotifications(Long userId, boolean unreadOnly);

    long getUnreadCount(Long userId);

    NotificationResponse markAsRead(Long userId, Long notificationId);

    void markAllAsRead(Long userId);

    void deleteNotification(Long userId, Long notificationId);

    /** Internal API other services use to raise a notification for a user; not exposed over REST. */
    void notify(Long userId, NotificationType type, String title, String message);
}
