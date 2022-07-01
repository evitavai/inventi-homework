package com.inventi.homework.controller;

import com.inventi.homework.entity.BankAccount;
import com.inventi.homework.entity.BankAccountOperation;
import com.inventi.homework.repository.BankAccountOperationRepository;
import com.inventi.homework.repository.BankAccountRepository;
import com.inventi.homework.service.BankAccountOperationService;
import com.opencsv.CSVWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class BankAccountOperationControllerTest {

    @Autowired
    private BankAccountOperationRepository bankAccountOperationRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BankAccountOperationService bankAccountOperationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void tearDown() throws IOException {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "bank_account_operations", "bank_accounts");//deletes all data from table before each test

        String filename = "bankStatement.csv";

        List<BankAccountOperation> bankOperations = new ArrayList<>();
        List<BankAccount> bankAccounts = new ArrayList<>();


        BankAccount bankAccount = BankAccount.builder().accountNumber("TSG54SA").build();
        BankAccount bankAccount2 = BankAccount.builder().accountNumber("JHADD54").build();

        bankAccountRepository.saveAndFlush(bankAccount);
        bankAccountRepository.saveAndFlush(bankAccount2);


        bankOperations.add(BankAccountOperation.builder().accountNumber(bankAccount.getAccountNumber()).operationDate(Timestamp.from(Instant.now()))
            .beneficiary("Jane Doe")
            .amount(BigDecimal.TEN)
            .currency("EUR")
            .build());

        bankOperations.add(BankAccountOperation.builder().accountNumber(bankAccount2.getAccountNumber()).operationDate(Timestamp.from(Instant.now()))
            .beneficiary("Joe Doe")
            .amount(BigDecimal.TEN)
            .currency("USD")
            .build());

        String csv = "test-data.csv";

        StringWriter sw = new StringWriter();
        CSVWriter writer = new CSVWriter(new FileWriter(csv));

        //Write header
        String[] header = {"account_number", "operation_date", "beneficiary", "comment", "amount", "currency"};
        writer.writeNext(header);

        //Write data

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String[] data;
        for (BankAccountOperation s : bankOperations) {
            data = new String[]{String.valueOf(s.getAccountNumber()), df.format(s.getOperationDate()), s.getBeneficiary(), s.getComment(), String.valueOf(s.getAmount()), s.getCurrency()};
            writer.writeNext(data);
        }
        writer.close();
    }

    @Test
    void importsBankStatementFromCsvFileSuccessfully() throws IOException {

        MockMultipartFile mockitoMultipartFile = new MockMultipartFile("test", Files.newInputStream(Paths.get("/Users/evita/inventi-homework/test-data.csv")));

        bankAccountOperationService.importCSV(mockitoMultipartFile);

        List<BankAccountOperation> importedTransactions = bankAccountOperationRepository.findAll();

        assertEquals(2, importedTransactions.size());
        assertEquals("TSG54SA", importedTransactions.get(0).getAccountNumber());
        assertEquals("JHADD54", importedTransactions.get(1).getAccountNumber());
    }

    @Test
    void exportsBankStatementToCsvFileSuccessfully() throws IOException {

        MockMultipartFile mockitoMultipartFile = new MockMultipartFile("test", Files.newInputStream(Paths.get("/Users/evita/inventi-homework/test-data.csv")));

        bankAccountOperationService.importCSV(mockitoMultipartFile);

        List<BankAccountOperation> importedTransactions = bankAccountOperationRepository.findAll();

        assertEquals(2, importedTransactions.size());
        assertEquals("TSG54SA", importedTransactions.get(0).getAccountNumber());
        assertEquals("JHADD54", importedTransactions.get(1).getAccountNumber());
    }

}