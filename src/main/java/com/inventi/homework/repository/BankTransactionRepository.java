package com.inventi.homework.repository;

import com.inventi.homework.model.BankTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BankTransactionRepository extends PagingAndSortingRepository<BankTransaction, Long> {

    @Query(value = "SELECT * FROM bank_account_transactions WHERE account_number = ?1 AND transaction_date BETWEEN ?2 AND ?3 ORDER BY account_number", nativeQuery = true)
    Optional<List<BankTransaction>> findByAccountBalancesBetween(String accountNumber, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);

    @Query(value = "SELECT * FROM bank_account_transactions WHERE account_number = ?1 AND transaction_date = ?2 AND beneficiary = ?3 " +
        "AND comment = ?4 AND amount = ?5 AND currency = ?6 AND is_withdrawal = ?7", nativeQuery = true)
    Optional<BankTransaction> findBankTransactionExactMatch(String accountNumber, LocalDateTime transactionDate,
                                                            String beneficiary, String comment, BigDecimal amount, String currency, boolean isWithdrawal);

}
