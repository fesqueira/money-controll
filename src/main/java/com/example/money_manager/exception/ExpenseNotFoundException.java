package com.example.money_manager.exception;

public class ExpenseNotFoundException extends RuntimeException{
    public   ExpenseNotFoundException(Long id){
        super("Expense not found with id:" + id);
    }
}
