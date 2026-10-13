package com.projects.money_controll.dto;

import com.projects.money_controll.model.Transaction;
import com.projects.money_controll.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        String description,
        BigDecimal amount,
        TransactionType type,
        String category,
        LocalDate date,
        Instant createdAt) {

    public static TransactionResponse of(Transaction t) {
        return new TransactionResponse(t.getId(), t.getDescription(), t.getAmount(), t.getType(),
                t.getCategory(), t.getDate(), t.getCreatedAt());
    }
}
