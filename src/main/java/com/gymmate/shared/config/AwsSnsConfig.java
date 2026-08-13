package com.gymmate.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

/**
 * Configures the AWS SnsClient bean.
 * Uses explicit credentials when provided (dev/CI); falls back to the AWS Default
 * Credentials Provider Chain (IAM role, instance profile, etc.) in production.
 */
@Configuration
@Slf4j
public class AwsSnsConfig {

  @Value("${aws.region:us-east-1}")
  private String region;

  @Value("${aws.access-key-id:}")
  private String accessKeyId;

  @Value("${aws.secret-access-key:}")
  private String secretAccessKey;

  @Bean
  public SnsClient snsClient() {
    Region awsRegion = Region.of(region);

    if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(secretAccessKey)) {
      log.info("SnsClient: using explicit credentials, region={}", region);
      return SnsClient.builder()
          .region(awsRegion)
          .credentialsProvider(StaticCredentialsProvider.create(
              AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
          .build();
    }

    log.info("SnsClient: using default credentials provider chain, region={}", region);
    return SnsClient.builder()
        .region(awsRegion)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
  }
}
