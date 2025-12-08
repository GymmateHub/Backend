package com.gymmate.user.api.dto;

import java.math.BigDecimal;

public record TrainerUpdateRequest(
    BigDecimal hourlyRate,
    BigDecimal commissionRate,
    String bio,
    String[] specializations,
    String availabilityJson,
    String certificationsJson
) {}
