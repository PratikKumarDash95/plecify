package com.campusconnect.portal.service.notification;

import com.campusconnect.portal.common.enums.NotificationType;
import com.campusconnect.portal.entity.Notification;
import com.campusconnect.portal.entity.User;
import com.campusconnect.portal.repository.NotificationRepository;
import com.campusconnect.portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Default {@link NotificationService}. Uses a {@code getReferenceById} proxy for the
 * recipient to avoid a redundant load — only the FK is needed to persist the row.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void notify(UUID recipientId, NotificationType type, String title, String body, String link) {
        User recipient = userRepository.getReferenceById(recipientId);
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .body(body)
                .link(link)
                .build();
        notificationRepository.save(notification);
        log.debug("Notification '{}' created for user {}", type, recipientId);
    }
}
