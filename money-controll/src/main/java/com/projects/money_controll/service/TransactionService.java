package com.projects.money_controll.service;

import com.projects.money_controll.dto.CategoryTotal;
import com.projects.money_controll.dto.PageResponse;
import com.projects.money_controll.dto.SummaryResponse;
import com.projects.money_controll.dto.TransactionRequest;
import com.projects.money_controll.dto.TransactionResponse;
import com.projects.money_controll.model.Transaction;
import com.projects.money_controll.model.TransactionType;
import com.projects.money_controll.repository.TransactionRepository;
import com.projects.money_controll.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private static final Set<String> SORTABLE = Set.of("id", "date", "amount", "type", "category", "description", "createdAt");
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final TransactionRepository transactions;
    private final UserRepository users;

    public PageResponse<TransactionResponse> search(Long userId, TransactionType type, String category,
                                                    LocalDate from, LocalDate to, Pageable pageable) {
        validateRange(from, to);
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE.contains(order.getProperty())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cannot sort by '" + order.getProperty() + "'. Allowed: " + SORTABLE.stream().sorted().toList());
            }
        }

        Specification<Transaction> filter = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), normalizeCategory(category)));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), to));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return PageResponse.of(transactions.findAll(filter, pageable).map(TransactionResponse::of));
    }

    public TransactionResponse get(Long userId, Long id) {
        return TransactionResponse.of(findOwned(userId, id));
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setUser(users.getReferenceById(userId));
        apply(transaction, request);
        return TransactionResponse.of(transactions.save(transaction));
    }

    @Transactional
    public TransactionResponse update(Long userId, Long id, TransactionRequest request) {
        Transaction transaction = findOwned(userId, id);
        apply(transaction, request);
        return TransactionResponse.of(transaction);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        transactions.delete(findOwned(userId, id));
    }

    /** Totals for the given period; a missing bound defaults to the start/end of the current month. */
    public SummaryResponse summarize(Long userId, LocalDate from, LocalDate to) {
        YearMonth currentMonth = YearMonth.now();
        from = from != null ? from : currentMonth.atDay(1);
        to = to != null ? to : currentMonth.atEndOfMonth();
        validateRange(from, to);

        List<CategoryTotal> byCategory = transactions.sumByCategory(userId, from, to);
        BigDecimal income = total(byCategory, TransactionType.INCOME);
        BigDecimal expense = total(byCategory, TransactionType.EXPENSE);
        return new SummaryResponse(from, to, income, expense, income.subtract(expense), byCategory);
    }

    // A transaction owned by someone else is reported as not found, so ids can't be probed.
    private Transaction findOwned(Long userId, Long id) {
        return transactions.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction " + id + " not found"));
    }

    private static void apply(Transaction transaction, TransactionRequest request) {
        transaction.setDescription(request.description().trim());
        // @Digits guarantees at most two decimals, so this never rounds.
        transaction.setAmount(request.amount().setScale(2));
        transaction.setType(request.type());
        transaction.setCategory(normalizeCategory(request.category()));
        transaction.setDate(request.date() != null ? request.date() : LocalDate.now());
    }

    // Stored lower-case so "Food" and "food" are summed together.
    private static String normalizeCategory(String category) {
        return category.trim().toLowerCase(Locale.ROOT);
    }

    private static BigDecimal total(List<CategoryTotal> totals, TransactionType type) {
        return totals.stream()
                .filter(t -> t.type() == type)
                .map(CategoryTotal::total)
                .reduce(ZERO, BigDecimal::add);
    }

    private static void validateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' must not be after 'to'");
        }
    }
}
