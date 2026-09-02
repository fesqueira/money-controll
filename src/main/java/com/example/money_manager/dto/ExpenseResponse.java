package com.example.money_manager.dto;

import com.example.money_manager.model.Category;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(Long id, String description, BigDecimal amount, LocalDate date, Category category) {}
