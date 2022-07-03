package com.inventi.homework.service;

import com.inventi.homework.helpers.TestHelper;
import com.inventi.homework.model.BankTransaction;
import com.inventi.homework.repository.BankTransactionRepository;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public
class BankTransactionServiceTest {

    static public final String absoluteTestDataPath = new File("./data/").getAbsolutePath();
    @Autowired
    private BankTransactionRepository bankTransactionRepository;
    @Autowired
    private BankTransactionService bankTransactionService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private MockMultipartFile mockMultipartFile;

    @Autowired
    private Environment env;

    @Autowired
    TestHelper testHelper;

    @BeforeEach
    void setup() throws IOException {
        FileUtils.deleteDirectory(new File(absoluteTestDataPath));
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "bank_account_transactions", "bank_accounts");//deletes all data from table before each test

        var bankAccountNumbers = List.of("TSG54SA", "JHADD54");

        testHelper.createTestBankAccounts(bankAccountNumbers);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<BankTransaction> bankOperations = new ArrayList<>();
        bankOperations.add(BankTransaction.builder().accountNumber("TSG54SA").transactionDate(LocalDate.parse("2002-03-14", dateTimeFormatter).atStartOfDay())
            .beneficiary("Jane Doe")
            .amount(BigDecimal.valueOf(20))
            .currency("EUR")
            .build());
        bankOperations.add(BankTransaction.builder().accountNumber("TSG54SA").transactionDate(LocalDate.parse("2019-03-14", dateTimeFormatter).atStartOfDay())
            .beneficiary("Jane Doe")
            .amount(BigDecimal.TEN)
            .isWithdrawal(true)
            .currency("USD")
            .build());
        bankOperations.add(BankTransaction.builder().accountNumber("JHADD54").transactionDate(LocalDate.parse("2004-03-14", dateTimeFormatter).atStartOfDay())
            .beneficiary("Joe Doe")
            .amount(BigDecimal.TEN)
            .currency("USD")
            .build());

        testHelper.createTestBankTransactionData(bankOperations);

        mockMultipartFile = new MockMultipartFile("file", Files.newInputStream(Paths.get(Objects.requireNonNull(env.getProperty("testDataFile.path"))).toAbsolutePath()));
    }

    @Test
    void importsBankStatementFromCsvFileSuccessfully() {
        bankTransactionService.importCSV(mockMultipartFile);

        List<BankTransaction> importedTransactions = (List<BankTransaction>) bankTransactionRepository.findAll();

        assertEquals(3, importedTransactions.size());
        assertEquals("TSG54SA", importedTransactions.get(0).getAccountNumber());
        assertEquals("TSG54SA", importedTransactions.get(1).getAccountNumber());
        assertEquals("JHADD54", importedTransactions.get(2).getAccountNumber());
        assertEquals(BigDecimal.valueOf(20), importedTransactions.get(0).getAmount());
        assertEquals(BigDecimal.TEN.negate(), importedTransactions.get(1).getAmount());

    }

    @Test
    void calculatesBalanceSuccessfully() throws ParseException {
        bankTransactionService.importCSV(mockMultipartFile);

        BigDecimal balance = bankTransactionService.calculateAccountBalance("TSG54SA", "2001-05-06", "2022-08-06", PageRequest.of(0, 5));

        assertEquals(BigDecimal.TEN, balance);
    }

}