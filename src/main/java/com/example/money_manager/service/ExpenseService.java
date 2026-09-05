package com.example.money_manager.service;

import com.example.money_manager.dto.ExpenseRequest;
import com.example.money_manager.dto.ExpenseResponse;
import com.example.money_manager.exception.ExpenseNotFoundException;
import com.example.money_manager.model.Expense;
import com.example.money_manager.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


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

    public ExpenseResponse findById(Long id){
        Expense expense = repository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));

        return toResponse(expense);

    }

    public ExpenseResponse update(Long id, ExpenseRequest request){
        Expense expense = repository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));


        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setCategory(request.category());
        expense.setDate(request.date());
        Expense updated = repository.save(expense);

        return this.toResponse(updated);

    }

    public void delete(Long id){
        if (!repository.existsById(id)){
            throw new ExpenseNotFoundException(id);
        }
        repository.deleteById(id);
    }

}
