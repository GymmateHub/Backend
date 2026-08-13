package com.gymmate.notification.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.notification.application.channel.SnsChannelSender;
import com.gymmate.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Publishes dispatched notifications to AWS SNS.
 *
 * <p>Only active when {@code aws.sns.enabled=true}. All publish failures are
 * caught and logged — SNS errors never interrupt the primary SSE dispatch path.
 */
@Service
@ConditionalOnProperty(name = "aws.sns.enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
public class SnsNotificationPublisher {

  private final SnsChannelSender snsChannelSender;
  private final ObjectMapper objectMapper;

  /**
   * Publish a notification to the default SNS topic.
   *
   * @param notification the notification to publish
   */
  public boolean publish(Notification notification) {
    try {
      String message = buildMessage(notification);
      String subject = truncate(notification.getTitle(), 100);
      String messageId = snsChannelSender.publishToTopic(null, subject, message);
      log.info("Notification {} published to SNS. MessageId={}", notification.getId(), messageId);
    return true;
  } catch (Exception e) {
    log.error("Failed to publish notification {} to SNS — continuing: {}",
        notification.getId(), e.getMessage());
    return false;
  }
  }

  private String buildMessage(Notification notification) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("notificationId", notification.getId());
    payload.put("title", notification.getTitle());
    payload.put("message", notification.getMessage());
    payload.put("priority", notification.getPriority());
    payload.put("eventType", notification.getEventType());
    payload.put("scope", notification.getScope());
    payload.put("organisationId", notification.getOrganisationId());
    payload.put("gymId", notification.getGymId());
    payload.put("recipientRole", notification.getRecipientRole());

    try {
      return objectMapper.writeValueAsString(payload);
    } catch (Exception e) {
      log.warn("Could not serialize notification payload to JSON, using plain text fallback");
      return notification.getTitle() + ": " + notification.getMessage();
    }
  }

  /** SNS subject line is capped at 100 characters. */
  private String truncate(String value, int maxLength) {
    if (value == null) return "";
    return value.length() <= maxLength ? value : value.substring(0, maxLength);
  }
}
