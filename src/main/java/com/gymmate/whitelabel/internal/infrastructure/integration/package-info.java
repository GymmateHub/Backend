/**
 * TRANSITIONAL grant (clean-architecture refactor, Step C removes it): notification builds tenant mail senders via DynamicMailSenderFactory until whitelabel.api exposes a mail-sender facade.
 */
@org.springframework.modulith.NamedInterface("integration")
package com.gymmate.whitelabel.internal.infrastructure.integration;
