package com.projects.money_controll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SummaryResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        List<CategoryTotal> byCategory) {
}
