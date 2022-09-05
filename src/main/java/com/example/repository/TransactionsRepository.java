package com.example.repository;

import com.example.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;

@Repository
public interface TransactionsRepository extends JpaRepository<Transaction, Long> {

    @Query(value = "SELECT SUM(amount) FROM transaction  WHERE client_id = ?1 AND to_char(date, 'YYYY-MM') = ?2", nativeQuery = true)
    BigDecimal getClientSumOfTurnoverPerMonth(int client_id, String yearMonth);

    Transaction findByIdAndDate(int id, Date date);
}
