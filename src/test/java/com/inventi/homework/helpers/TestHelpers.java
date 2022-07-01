package com.inventi.homework.helpers;

import com.inventi.homework.entity.BankAccount;
import com.inventi.homework.entity.BankAccountOperation;
import com.inventi.homework.repository.BankAccountRepository;
import com.opencsv.CSVWriter;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public final class TestHelpers {
    public static void createTestBankAccounts(ArrayList<String> accountNumbers, BankAccountRepository bankAccountRepository) {
        accountNumbers.forEach((accountNumber) -> {
            bankAccountRepository.saveAndFlush(BankAccount.builder().accountNumber(accountNumber).build());
        });
    }

    public static void createTestBankTransactionData(List<BankAccountOperation> bankAccountTransactions) throws IOException {
        File csvOutputFile = new File("/Users/evita/inventi-homework/src/test/java/com/inventi/homework/data/test-data.csv");
        csvOutputFile.getParentFile().mkdirs();

        CSVWriter csvWriter = new CSVWriter(new FileWriter(csvOutputFile));
        String[] header = {"account_number", "operation_date", "beneficiary", "comment", "amount", "currency", "is_withdrawal"};
        csvWriter.writeNext(header);
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String[] csvData;
        for (BankAccountOperation bankTransaction : bankAccountTransactions) {
            csvData = new String[]{String.valueOf(bankTransaction.getAccountNumber()), dateFormatter.format(bankTransaction.getOperationDate()),
                bankTransaction.getBeneficiary(), bankTransaction.getComment(), String.valueOf(bankTransaction.getAmount()), bankTransaction.getCurrency(), String.valueOf(bankTransaction.isWithdrawal())};
            csvWriter.writeNext(csvData);
        }
        csvWriter.close();
    }
}