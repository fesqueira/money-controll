package com.projects.money_controll.controller;

import com.projects.money_controll.dto.SummaryResponse;
import com.projects.money_controll.security.AppUserDetails;
import com.projects.money_controll.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final TransactionService transactionService;

    @GetMapping
    public SummaryResponse summary(@AuthenticationPrincipal AppUserDetails user,
                                   @RequestParam(required = false) LocalDate from,
                                   @RequestParam(required = false) LocalDate to) {
        return transactionService.summarize(user.getId(), from, to);
    }
}
