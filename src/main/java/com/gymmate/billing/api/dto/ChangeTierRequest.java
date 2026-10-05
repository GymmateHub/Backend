package com.gymmate.billing.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeTierRequest {

    @NotBlank(message = "New tier name is required")
    private String newTierName;
}

