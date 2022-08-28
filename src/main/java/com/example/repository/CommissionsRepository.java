package com.example.repository;

import com.example.model.CommissionType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;

@Repository
public interface CommissionsRepository extends CrudRepository<CommissionType, Integer> {

    @Query(value = "SELECT SUM(amount) FROM commission_type  WHERE client_id = ?1 AND to_char(date, 'YYYY-MM') = ?2", nativeQuery = true)
    BigDecimal getClientSumOfTurnoverPerMonth(int client_id, String yearMonth);

    CommissionType findByIdAndDate(int id, Date date);
}
