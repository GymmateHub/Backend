package com.gymmate.notification.application.channel;

import com.gymmate.notification.domain.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

/**
 * PUSH channel sender backed by AWS SNS.
 *
 * <p>Routing:
 * <ul>
 *   <li>If {@code recipient} starts with {@code arn:} it is used as the target ARN directly
 *       (topic or platform endpoint).</li>
 *   <li>Otherwise the configured default topic ARN ({@code GYMMATE_SNS_TOPIC_ARN}) is used.</li>
 * </ul>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SnsChannelSender implements ChannelSender {

  private final SnsClient snsClient;

  @Value("${aws.sns.default-topic-arn:}")
  private String defaultTopicArn;

  @Override
  public NotificationChannel getChannel() {
    return NotificationChannel.PUSH;
  }

  @Override
  public void send(String recipient, String subject, String body) throws ChannelException {
    String targetArn = StringUtils.hasText(recipient) && recipient.startsWith("arn:")
        ? recipient
        : defaultTopicArn;

    if (!StringUtils.hasText(targetArn)) {
      throw new ChannelException(NotificationChannel.PUSH,
          "No SNS target ARN available. Set GYMMATE_SNS_TOPIC_ARN or pass an ARN as recipient.");
    }

    try {
      PublishResponse response = snsClient.publish(PublishRequest.builder()
          .topicArn(targetArn)
          .subject(subject)
          .message(body)
          .build());

      log.info("SNS message published. MessageId={}, target={}", response.messageId(), targetArn);

    } catch (SnsException e) {
      log.error("SNS publish failed to {}: {} ({})",
          targetArn, e.awsErrorDetails().errorMessage(), e.awsErrorDetails().errorCode());
      throw new ChannelException(NotificationChannel.PUSH,
          "SNS publish failed: " + e.awsErrorDetails().errorMessage(), e);
    }
  }

  /**
   * Broadcast directly to a topic ARN, bypassing the ChannelSender abstraction.
   * Pass {@code null} as {@code topicArn} to use the configured default topic.
   *
   * @return the SNS MessageId
   */
  public String publishToTopic(String topicArn, String subject, String message) {
    String resolvedArn = (topicArn != null) ? topicArn : defaultTopicArn;
    if (!org.springframework.util.StringUtils.hasText(resolvedArn)) {
      throw new RuntimeException("No SNS topic ARN provided and GYMMATE_SNS_TOPIC_ARN is not set.");
    }
    try {
      PublishResponse response = snsClient.publish(PublishRequest.builder()
          .topicArn(resolvedArn)
          .subject(subject)
          .message(message)
          .build());
      log.info("SNS broadcast published. MessageId={}, topic={}", response.messageId(), resolvedArn);
      return response.messageId();
    } catch (SnsException e) {
      log.error("SNS broadcast failed to {}: {}", resolvedArn, e.awsErrorDetails().errorMessage());
      throw new RuntimeException("SNS broadcast failed: " + e.awsErrorDetails().errorMessage(), e);
    }
  }
}
