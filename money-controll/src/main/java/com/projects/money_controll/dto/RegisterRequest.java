package com.projects.money_controll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotNull
        @Pattern(regexp = "^[a-zA-Z0-9._-]{3,30}$", message = "must be 3-30 letters, digits, '.', '_' or '-'")
        String username,
        // BCrypt only considers the first 72 bytes.
        @NotBlank @Size(min = 8, max = 72) String password) {
}
