package com.example.money_manager.service;

import com.example.money_manager.dto.ExpenseRequest;
import com.example.money_manager.dto.ExpenseResponse;
import com.example.money_manager.model.Expense;
import com.example.money_manager.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ExpenseService {
    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository){
        this.repository = repository;
    }

    private Expense toEntity (ExpenseRequest expenseRequest){
        Expense expense = new Expense();
        expense.setAmount(expenseRequest.amount());
        expense.setCategory(expenseRequest.category());
        expense.setDate(expenseRequest.date());
        expense.setDescription(expenseRequest.description());

        return expense;
    }

    private ExpenseResponse toResponse(Expense expense){
        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getDate(),
                expense.getCategory()
        );

    }

    public ExpenseResponse create(ExpenseRequest expenseRequest){
        Expense entityMod = toEntity(expenseRequest);
        Expense saved = repository.save(entityMod);
        return toResponse(saved);

    }

    public List<ExpenseResponse> findAll(){
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

}
