package com.inventi.homework.service;

import com.inventi.homework.entity.BankTransaction;
import com.inventi.homework.model.BankTransactionStatementModel;
import com.inventi.homework.repository.BankTransactionRepository;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.inventi.homework.helpers.BankTransactionHelpers.checkExistingBankTransaction;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankTransactionServiceImpl implements BankTransactionService {

    private final BankTransactionRepository bankTransactionRepository;

    @Override
    public void importCSV(MultipartFile file) {
        try {
            log.debug("Starting to read file {}", file.getName());
            InputStreamReader streamReader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
            CsvToBean<BankTransaction> csvToBean = new CsvToBeanBuilder<BankTransaction>(streamReader)
                .withType(BankTransaction.class)
                .withSkipLines(1)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

            List<BankTransaction> bankTransactionList = csvToBean.parse();

            bankTransactionList.forEach((bankTransaction) -> {
                checkExistingBankTransaction(bankTransaction, bankTransactionRepository);
                if (bankTransaction.isWithdrawal()) {
                    bankTransaction.setAmount(bankTransaction.getAmount().negate());
                }
            });

            bankTransactionRepository.saveAll(bankTransactionList);

            log.debug("File data saved successfully!");

        } catch (IOException e) {
            log.error("Unable to read or parse data", e);
        }

    }

    @Override
    public void exportCSV(BankTransactionStatementModel bankAccountStatement) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime formattedDateFrom = LocalDate.parse(bankAccountStatement.getDateFrom(), dateFormatter).atStartOfDay();
            LocalDateTime formattedDateTo = LocalDate.parse(bankAccountStatement.getDateTo(), dateFormatter).atStartOfDay();

            List<Optional<List<BankTransaction>>> bankTransactionList = new ArrayList<>();

            bankAccountStatement.getAccountNumber().forEach((accountNumber) -> {

                log.debug("Retrieving bank transactions for accountNumber {} between dates {} and {}", accountNumber, formattedDateFrom, formattedDateTo);


                Optional<List<BankTransaction>> existingBankTransactions = bankTransactionRepository.findByAccountBalancesBetween(accountNumber, formattedDateFrom, formattedDateTo);
                existingBankTransactions.ifPresent((bankTransactions) -> bankTransactionList.add(Optional.of(bankTransactions)));
            });

            File csvOutputFile = new File("/Users/evita/inventi-homework/src/test/java/com/inventi/homework/data/bank-statement-output.csv");
            csvOutputFile.getParentFile().mkdirs();

            log.debug("Starting to write data into an output file {}", csvOutputFile);


            CSVWriter csvWriter = new CSVWriter(new FileWriter(csvOutputFile));

            String[] header = {"account_number", "operation_date", "beneficiary", "comment", "amount", "currency", "is_withdrawal"};
            csvWriter.writeNext(header);

            String[] data;
            for (BankTransaction s : bankTransactionList
                .stream()
                .flatMap(Optional::stream)
                .findFirst().orElseThrow(() -> new Exception("No bank transactions found"))) {
                data = new String[]{String.valueOf(s.getAccountNumber()), String.valueOf(s.getTransactionDate()),
                    s.getBeneficiary(), s.getComment(), String.valueOf(s.getAmount()), s.getCurrency(), String.valueOf(s.isWithdrawal())};
                csvWriter.writeNext(data);
            }
            csvWriter.close();

            log.debug("Data written successfully to {}", csvOutputFile);

        } catch (Exception e) {
            log.error("Unable to read or parse data", e);
        }
    }

    @Override
    public BigDecimal calculateAccountBalance(String accountNumber, String dateFrom, String dateTo) throws ParseException {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime formattedDateFrom = LocalDate.parse(dateFrom, dateFormatter).atStartOfDay();
            LocalDateTime formattedDateTo = LocalDate.parse(dateTo, dateFormatter).atStartOfDay();

            log.debug("Retrieving bank transactions for accountNumber {} between dates {} and {}", accountNumber, formattedDateFrom, formattedDateTo);

            Optional<List<BankTransaction>> existingBankTransactions = bankTransactionRepository.findByAccountBalancesBetween(accountNumber, formattedDateFrom, formattedDateTo);

            BigDecimal sum = BigDecimal.ZERO;

            log.debug("Adding up amounts...");


            if (existingBankTransactions.isPresent()) {
                for (BankTransaction amt : existingBankTransactions.orElseThrow(() -> new Exception("No bank transactions found"))) {
                    sum = sum.add(amt.getAmount());
                }
            }

            log.debug("Transaction balance sum = {}", sum);


            return sum;


        } catch (Exception e) {
            log.error("Unable to parse data", e);
            throw new ParseException("Error while parsing given date", 0);
        }

    }

}
