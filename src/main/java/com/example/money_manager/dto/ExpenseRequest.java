package com.example.money_manager.dto;

import com.example.money_manager.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotBlank String description,
        @NotNull @Positive BigDecimal amount,
        @NotNull LocalDate date,
        @NotNull Category category
) {}
