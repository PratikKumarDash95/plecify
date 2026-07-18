package com.campusconnect.portal.service.notification;

import com.campusconnect.portal.common.enums.NotificationType;

import java.util.UUID;

/**
 * Creates in-app notifications addressed to a user. Persistence is on the caller's
 * transaction so a notification is written atomically with the event that produced it;
 * email side-channels (if any) are dispatched separately and asynchronously.
 */
public interface NotificationService {

    /**
     * Records a single in-app notification.
     *
     * @param recipientId the target user's id
     * @param type        notification category
     * @param title       short headline
     * @param body        notification body
     * @param link        optional client deep link (may be null)
     */
    void notify(UUID recipientId, NotificationType type, String title, String body, String link);
}
