package com.example.money_manager.controller;


import com.example.money_manager.dto.ExpenseRequest;
import com.example.money_manager.dto.ExpenseResponse;
import com.example.money_manager.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service){
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@RequestBody @Valid ExpenseRequest request){
        ExpenseResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> findAll(){
        List<ExpenseResponse> result = service.findAll();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> findById(@PathVariable Long id){
        ExpenseResponse response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(@PathVariable Long id,@RequestBody @Valid ExpenseRequest request){
        ExpenseResponse response = service.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
