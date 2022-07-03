package com.inventi.homework.helpers;

import com.inventi.homework.model.BankAccount;
import com.inventi.homework.model.BankTransaction;
import com.inventi.homework.repository.BankAccountRepository;
import com.opencsv.CSVWriter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Slf4j
@Component
public class TestHelper {

    private final BankAccountRepository bankAccountRepository;

    private final Environment env;

    public void createTestBankAccounts(List<String> accountNumbers) {
        accountNumbers.forEach((accountNumber) -> {
            bankAccountRepository.saveAndFlush(BankAccount.builder().accountNumber(accountNumber).build());
        });
    }

    public void createTestBankTransactionData(List<BankTransaction> bankAccountTransactions) {
        try {
            File csvOutputFile = new File(Objects.requireNonNull(env.getProperty("testDataFile.path")));
            csvOutputFile.getParentFile().mkdirs();

            CSVWriter csvWriter = new CSVWriter(new FileWriter(csvOutputFile));
            String[] header = {"account_number", "operation_date", "beneficiary", "comment", "amount", "currency", "is_withdrawal"};
            csvWriter.writeNext(header);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String[] csvData;
            for (BankTransaction bankTransaction : bankAccountTransactions) {
                csvData = new String[]{String.valueOf(bankTransaction.getAccountNumber()), bankTransaction.getTransactionDate().format(dateFormatter),
                    bankTransaction.getBeneficiary(), bankTransaction.getComment(), String.valueOf(bankTransaction.getAmount()), bankTransaction.getCurrency(), String.valueOf(bankTransaction.isWithdrawal())};
                csvWriter.writeNext(csvData);
            }
            csvWriter.close();
        } catch (IOException e) {
            log.error("Unable to read or parse data", e);
        }
    }
}