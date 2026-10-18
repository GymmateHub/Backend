package com.gymmate.whitelabel.api.dto;

/**
 * Non-secret whitelabel configuration of a tenant (gym settings override organisation
 * settings), exposed to other modules for branding and channel selection.
 */
public record WhitelabelProfile(
        String brandName,
        String logoUrl,
        String primaryColor,
        String secondaryColor,
        String supportEmail,
        String supportPhone,
        String emailFooterText,
        boolean smtpEnabled,
        String smtpFromEmail,
        String smtpFromName,
        String smtpUsername,
        boolean whatsappEnabled) {
}
