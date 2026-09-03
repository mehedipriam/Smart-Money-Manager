package com.smartmoneymanager.backend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmoneymanager.backend.dto.response.NotificationResponse;
import com.smartmoneymanager.backend.entity.Notification;
import com.smartmoneymanager.backend.entity.enums.NotificationType;
import com.smartmoneymanager.backend.exception.ResourceNotFoundException;
import com.smartmoneymanager.backend.mapper.NotificationMapper;
import com.smartmoneymanager.backend.repository.NotificationRepository;
import com.smartmoneymanager.backend.repository.UserRepository;
import com.smartmoneymanager.backend.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId, boolean unreadOnly) {
        List<Notification> notifications = unreadOnly
                ? notificationRepository.findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                : notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream().map(notificationMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public NotificationResponse markAsRead(Long userId, Long notificationId) {
        Notification notification = findOwned(userId, notificationId);
        notification.setRead(true);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Override
    public void deleteNotification(Long userId, Long notificationId) {
        notificationRepository.delete(findOwned(userId, notificationId));
    }

    @Override
    public void notify(Long userId, NotificationType type, String title, String message) {
        Notification notification = Notification.builder()
                .user(userRepository.getReferenceById(userId))
                .type(type)
                .title(title)
                .message(message)
                .build();
        notificationRepository.save(notification);
    }

    private Notification findOwned(Long userId, Long notificationId) {
        return notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
    }
}
