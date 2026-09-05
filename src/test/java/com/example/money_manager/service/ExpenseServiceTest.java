package com.example.money_manager.service;

import com.example.money_manager.dto.ExpenseRequest;
import com.example.money_manager.dto.ExpenseResponse;
import com.example.money_manager.exception.ExpenseNotFoundException;
import com.example.money_manager.model.Category;
import com.example.money_manager.model.Expense;
import com.example.money_manager.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository repository;

    @InjectMocks
    private ExpenseService service;

    @Test
    void MustFindExpensesById() {
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setDescription("Mercado");
        expense.setAmount(new BigDecimal("150.50"));
        expense.setDate(LocalDate.of(2026, 9, 5));
        expense.setCategory(Category.ALIMENTACAO);

        when(repository.findById(1L)).thenReturn(Optional.of(expense));

        ExpenseResponse result = service.findById(1L);

        assertEquals("Mercado", result.description());
        assertEquals(1L, result.id());
    }

    @Test
    void MustThrowExceptionWhenListingVoid() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ExpenseNotFoundException.class, () -> service.findById(999L));
    }

    @Test
    void MustSuccessfullyCreateExpense() {
        ExpenseRequest request = new ExpenseRequest(
                "Mercado",
                new BigDecimal("150.50"),
                LocalDate.of(2026, 9, 5),
                Category.ALIMENTACAO
        );

        Expense savedExpense = new Expense();
        savedExpense.setId(1L);
        savedExpense.setDescription(request.description());
        savedExpense.setAmount(request.amount());
        savedExpense.setDate(request.date());
        savedExpense.setCategory(request.category());

        when(repository.save(any(Expense.class))).thenReturn(savedExpense);

        ExpenseResponse result = service.create(request);

        assertEquals(1L, result.id());
        assertEquals("Mercado", result.description());
        assertEquals(Category.ALIMENTACAO, result.category());
    }

    @Test
    void MustListAllExpenses() {
        Expense expense1 = new Expense();
        expense1.setId(1L);
        expense1.setDescription("Mercado");
        expense1.setAmount(new BigDecimal("150.50"));
        expense1.setDate(LocalDate.of(2026, 9, 5));
        expense1.setCategory(Category.ALIMENTACAO);

        Expense expense2 = new Expense();
        expense2.setId(2L);
        expense2.setDescription("Uber");
        expense2.setAmount(new BigDecimal("25.00"));
        expense2.setDate(LocalDate.of(2026, 9, 6));
        expense2.setCategory(Category.TRANSPORTE);

        when(repository.findAll()).thenReturn(List.of(expense1, expense2));

        List<ExpenseResponse> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("Mercado", result.get(0).description());
        assertEquals("Uber", result.get(1).description());
    }

    @Test
    void MustUpdateSuccessfully() {
        Expense existingExpense = new Expense();
        existingExpense.setId(1L);
        existingExpense.setDescription("Mercado");
        existingExpense.setAmount(new BigDecimal("150.50"));
        existingExpense.setDate(LocalDate.of(2026, 9, 5));
        existingExpense.setCategory(Category.ALIMENTACAO);

        ExpenseRequest updateRequest = new ExpenseRequest(
                "Mercado - compra do mês",
                new BigDecimal("210.90"),
                LocalDate.of(2026, 9, 6),
                Category.ALIMENTACAO
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existingExpense));
        when(repository.save(any(Expense.class))).thenReturn(existingExpense);

        ExpenseResponse result = service.update(1L, updateRequest);

        assertEquals("Mercado - compra do mês", result.description());
        assertEquals(new BigDecimal("210.90"), result.amount());
    }

    @Test
    void MustThrowExceptionWhenUpdatingVoid() {
        ExpenseRequest updateRequest = new ExpenseRequest(
                "Mercado",
                new BigDecimal("150.50"),
                LocalDate.of(2026, 9, 5),
                Category.ALIMENTACAO
        );

        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ExpenseNotFoundException.class, () -> service.update(999L, updateRequest));
    }

    @Test
    void MustDelete() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void mustThrowExceptionWhenDeletingVoid() {
        when(repository.existsById(999L)).thenReturn(false);

        assertThrows(ExpenseNotFoundException.class, () -> service.delete(999L));

        verify(repository, never()).deleteById(any());
    }
}