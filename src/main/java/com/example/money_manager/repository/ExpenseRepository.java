package com.example.money_manager.repository;

import com.example.money_manager.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


public interface ExpenseRepository extends JpaRepository<Expense, Long> {

   List<Expense> findByDateBetween(LocalDate startDate, LocalDate endDate);



}
