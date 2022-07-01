package com.inventi.homework.repository;

import com.inventi.homework.entity.BankAccountOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface BankAccountOperationRepository extends JpaRepository<BankAccountOperation, Long> {

    @Query(value = "SELECT * FROM bank_account_operations WHERE account_number = ?1 AND operation_date BETWEEN ?2 AND ?3", nativeQuery = true)
    List<BankAccountOperation> findByAccountBalancesBetween(String accountNumber, Timestamp dateFrom, Timestamp dateTo);

}
