package com.projects.money_controll.repository;

import com.projects.money_controll.dto.CategoryTotal;
import com.projects.money_controll.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    @Query("""
            select new com.projects.money_controll.dto.CategoryTotal(t.category, t.type, sum(t.amount))
            from Transaction t
            where t.user.id = :userId and t.date between :from and :to
            group by t.category, t.type
            order by sum(t.amount) desc
            """)
    List<CategoryTotal> sumByCategory(Long userId, LocalDate from, LocalDate to);
}
