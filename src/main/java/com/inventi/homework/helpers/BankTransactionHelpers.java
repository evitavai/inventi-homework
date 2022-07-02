package com.inventi.homework.helpers;

import com.inventi.homework.entity.BankTransaction;
import com.inventi.homework.repository.BankTransactionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@AllArgsConstructor
@Slf4j
public final class BankTransactionHelpers {
    public static void checkExistingBankTransaction(BankTransaction bankTransaction, BankTransactionRepository bankTransactionRepository) {
        Optional<BankTransaction> existingBankTransaction = bankTransactionRepository.findBankTransactionExactMatch(bankTransaction.getAccountNumber(),
            bankTransaction.getTransactionDate(), bankTransaction.getBeneficiary(), bankTransaction.getComment(), bankTransaction.getAmount(),
            bankTransaction.getCurrency(), bankTransaction.isWithdrawal());
        if (existingBankTransaction.isPresent()) {
            throw new IllegalArgumentException("Exact bank transaction record already exists");
        }
    }
}