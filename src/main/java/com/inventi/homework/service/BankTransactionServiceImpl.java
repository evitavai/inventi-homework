package com.inventi.homework.service;

import com.inventi.homework.controller.requestdto.BankTransactionStatementParams;
import com.inventi.homework.helpers.BankTransactionHelper;
import com.inventi.homework.model.BankTransaction;
import com.inventi.homework.repository.BankTransactionRepository;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
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
import java.util.Objects;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class BankTransactionServiceImpl implements BankTransactionService {

    private final BankTransactionRepository bankTransactionRepository;

    private final Environment env;

    private final BankTransactionHelper bankTransactionHelper;

    @Override
    public void importCSV(MultipartFile file) {
        try {
            log.debug("Starting to read file {}", file.getName());
            try (InputStreamReader streamReader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
                CsvToBean<BankTransaction> csvToBean = new CsvToBeanBuilder<BankTransaction>(streamReader)
                    .withType(BankTransaction.class)
                    .withSkipLines(1)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

                List<BankTransaction> bankTransactionList = csvToBean.parse();

                bankTransactionList.forEach((bankTransaction) -> {
                    bankTransactionHelper.checkExistingBankTransaction(bankTransaction);
                    if (bankTransaction.isWithdrawal()) {
                        bankTransaction.setAmount(bankTransaction.getAmount().negate());
                    }
                });

                bankTransactionRepository.saveAll(bankTransactionList);
                log.debug("File data saved successfully!");
            }

        } catch (IOException e) {
            log.error("Unable to read or parse data", e);
        }

    }

    @Override
    public void exportCSV(BankTransactionStatementParams bankAccountStatement, Pageable pageable) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime formattedDateFrom = LocalDate.parse(bankAccountStatement.getDateFrom(), dateFormatter).atStartOfDay();
            LocalDateTime formattedDateTo = LocalDate.parse(bankAccountStatement.getDateTo(), dateFormatter).atStartOfDay();

            List<Optional<List<BankTransaction>>> bankTransactionList = new ArrayList<>();

            bankAccountStatement.getAccountNumber().forEach((accountNumber) -> {
                log.debug("Retrieving bank transactions for accountNumber {} between dates {} and {}", accountNumber, formattedDateFrom, formattedDateTo);
                Optional<List<BankTransaction>> existingBankTransactions = bankTransactionRepository.findByAccountBalancesBetween(accountNumber, formattedDateFrom, formattedDateTo, pageable);
                existingBankTransactions.ifPresent((bankTransactions) -> bankTransactionList.add(Optional.of(bankTransactions)));
            });

            File file = new File(Objects.requireNonNull(env.getProperty("outputFile.path")));
            File csvOutputFile = new File(file.getAbsolutePath());
            csvOutputFile.getParentFile().mkdirs();

            log.debug("Starting to write data into an output file {}", csvOutputFile);

            try (CSVWriter csvWriter = new CSVWriter(new FileWriter(String.valueOf(csvOutputFile)))) {

                String[] header = {"account_number", "operation_date", "beneficiary", "comment", "amount", "currency", "is_withdrawal"};
                csvWriter.writeNext(header);

                String[] data;
                for (BankTransaction s : bankTransactionList
                    .stream()
                    .flatMap(Optional::stream)
                    .findFirst().orElseThrow(() -> new Exception("No bank transactions found"))) {
                    data = new String[]{String.valueOf(s.getAccountNumber()), s.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        s.getBeneficiary(), s.getComment(), String.valueOf(s.getAmount()), s.getCurrency(), String.valueOf(s.isWithdrawal())};
                    csvWriter.writeNext(data);
                }
            }

            log.debug("Data written successfully to {}", csvOutputFile);

        } catch (Exception e) {
            log.error("Unable to read or parse data", e);
        }
    }

    @Override
    public BigDecimal calculateAccountBalance(String accountNumber, String dateFrom, String dateTo, Pageable pageable) throws ParseException {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime formattedDateFrom = LocalDate.parse(dateFrom, dateFormatter).atStartOfDay();
            LocalDateTime formattedDateTo = LocalDate.parse(dateTo, dateFormatter).atStartOfDay();

            log.debug("Retrieving bank transactions for accountNumber {} between dates {} and {}", accountNumber, formattedDateFrom, formattedDateTo);
            Optional<List<BankTransaction>> existingBankTransactions = bankTransactionRepository.findByAccountBalancesBetween(accountNumber, formattedDateFrom, formattedDateTo, pageable);

            log.debug("Adding up amounts...");

            BigDecimal sum = existingBankTransactions.orElseThrow(() -> new Exception("No bank transactions found"))
                .stream().map(BankTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.debug("Transaction balance sum = {}", sum);
            return sum;
        } catch (Exception e) {
            log.error("Unable to parse data", e);
            throw new ParseException("Error while parsing given date", 0);
        }

    }

}
