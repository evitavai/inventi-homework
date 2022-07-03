package com.inventi.homework.helpers;

import com.inventi.homework.model.BankTransaction;
import com.inventi.homework.repository.BankTransactionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@AllArgsConstructor
@Slf4j
@Component
public final class BankTransactionHelper {

    private final BankTransactionRepository bankTransactionRepository;

    public void checkExistingBankTransaction(BankTransaction bankTransaction) {
        Optional<BankTransaction> existingBankTransaction = bankTransactionRepository.findBankTransactionExactMatch(bankTransaction.getAccountNumber(),
            bankTransaction.getTransactionDate(), bankTransaction.getBeneficiary(), bankTransaction.getComment(), bankTransaction.getAmount(),
            bankTransaction.getCurrency(), bankTransaction.isWithdrawal());
        if (existingBankTransaction.isPresent()) {
            throw new IllegalArgumentException("Exact bank transaction record already exists");
        }
    }
}