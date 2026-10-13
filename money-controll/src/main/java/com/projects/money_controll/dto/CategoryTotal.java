package com.projects.money_controll.dto;

import com.projects.money_controll.model.TransactionType;

import java.math.BigDecimal;

public record CategoryTotal(String category, TransactionType type, BigDecimal total) {
}
