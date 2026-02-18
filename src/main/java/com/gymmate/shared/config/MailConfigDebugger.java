package com.gymmate.shared.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MailConfigDebugger {

  @Value("${spring.mail.password:NOT_SET}")
  private String mailPassword;

  @Value("${spring.mail.username:NOT_SET}")
  private String mailUsername;

  @Value("${spring.mail.host:NOT_SET}")
  private String mailHost;

  @Value("${spring.mail.port:0}")
  private int mailPort;

  @PostConstruct
  public void logMailConfig() {
    log.info("=================================================");
    log.info("📧 MAIL CONFIGURATION DEBUG");
    log.info("=================================================");
    log.info("Host: {}", mailHost);
    log.info("Port: {}", mailPort);
    log.info("Username: {}", mailUsername);
    log.info("Password loaded: {}", mailPassword.equals("NOT_SET") ? "❌ NO" : "✅ YES");
    log.info("Password length: {}", mailPassword.equals("NOT_SET") ? 0 : mailPassword.length());
    log.info("Password first 3 chars: {}", mailPassword.equals("NOT_SET") ? "N/A" : mailPassword.substring(0, Math.min(3, mailPassword.length())) + "***");
    log.info("=================================================");
  }
}
