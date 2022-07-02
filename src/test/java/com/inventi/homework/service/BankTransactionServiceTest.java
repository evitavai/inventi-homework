package com.inventi.homework.service;

import com.inventi.homework.entity.BankTransaction;
import com.inventi.homework.model.BankTransactionStatementModel;
import com.inventi.homework.repository.BankAccountRepository;
import com.inventi.homework.repository.BankTransactionRepository;
import org.apache.tomcat.util.http.fileupload.FileUtils;
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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.inventi.homework.helpers.TestHelpers.createTestBankAccounts;
import static com.inventi.homework.helpers.TestHelpers.createTestBankTransactionData;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class BankTransactionServiceTest {

    @Autowired
    private BankTransactionRepository bankTransactionRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BankTransactionService bankTransactionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File("/Users/evita/inventi-homework/src/test/java/com/inventi/homework/data/"));
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "bank_account_transactions", "bank_accounts");//deletes all data from table before each test

        ArrayList<String> bankAccountNumbers = new ArrayList<>();
        bankAccountNumbers.add("TSG54SA");
        bankAccountNumbers.add("JHADD54");

        createTestBankAccounts(bankAccountNumbers, bankAccountRepository);
    }

    @Test
    void importsBankStatementFromCsvFileSuccessfully() throws IOException {
        List<BankTransaction> bankOperations = new ArrayList<>();
        bankOperations.add(BankTransaction.builder().accountNumber("TSG54SA").transactionDate(LocalDateTime.now())
            .beneficiary("Jane Doe")
            .amount(BigDecimal.valueOf(20))
            .currency("EUR")
            .build());
        bankOperations.add(BankTransaction.builder().accountNumber("JHADD54").transactionDate(LocalDateTime.now())
            .beneficiary("Joe Doe")
            .amount(BigDecimal.TEN)
            .currency("USD")
            .isWithdrawal(true)
            .build());

        createTestBankTransactionData(bankOperations);

        MockMultipartFile mockitoMultipartFile = new MockMultipartFile("test-data.csv", Files.newInputStream(Paths.get("/Users/evita/inventi-homework/src/test/java/com/inventi/homework/data/test-data.csv")));

        bankTransactionService.importCSV(mockitoMultipartFile);

        List<BankTransaction> importedTransactions = bankTransactionRepository.findAll();

        assertEquals(2, importedTransactions.size());
        assertEquals("TSG54SA", importedTransactions.get(0).getAccountNumber());
        assertEquals("JHADD54", importedTransactions.get(1).getAccountNumber());
    }

    @Test
    void exportsSingleAccountBankStatementToCsvFileSuccessfully() throws IOException, ParseException {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<BankTransaction> bankOperations = new ArrayList<>();
        bankOperations.add(BankTransaction.builder().accountNumber("TSG54SA").transactionDate(LocalDate.parse("2002-03-14", df).atStartOfDay())
            .beneficiary("Jane Doe")
            .amount(BigDecimal.valueOf(20))
            .currency("EUR")
            .build());
        bankOperations.add(BankTransaction.builder().accountNumber("TSG54SA").transactionDate(LocalDate.parse("2019-03-14", df).atStartOfDay())
            .beneficiary("Jane Doe")
            .amount(BigDecimal.TEN)
            .currency("USD")
            .build());
        bankOperations.add(BankTransaction.builder().accountNumber("JHADD54").transactionDate(LocalDate.parse("2004-03-14", df).atStartOfDay())
            .beneficiary("Joe Doe")
            .amount(BigDecimal.TEN)
            .currency("USD")
            .isWithdrawal(true)
            .build());

        createTestBankTransactionData(bankOperations);


        MockMultipartFile mockitoMultipartFile = new MockMultipartFile("test-data.csv", Files.newInputStream(Paths.get("/Users/evita/inventi-homework/src/test/java/com/inventi/homework/data/test-data.csv")));

        bankTransactionService.importCSV(mockitoMultipartFile);


        List<String> accountNumbers = new ArrayList<>();


        accountNumbers.add("TSG54SA");

        BankTransactionStatementModel bm = BankTransactionStatementModel.builder().accountNumber(accountNumbers).dateFrom("2001-05-06").dateTo("2022-08-06").build();


        bankTransactionService.exportCSV(bm);
    }

    @Test
    void exportsMultipleAccountBankStatementsToCsvFileSuccessfully() throws IOException, ParseException {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<BankTransaction> bankOperations = new ArrayList<>();
        bankOperations.add(BankTransaction.builder().accountNumber("TSG54SA").transactionDate(LocalDate.parse("2002-03-14", df).atStartOfDay())
            .beneficiary("Jane Doe")
            .amount(BigDecimal.valueOf(20))
            .currency("EUR")
            .build());
        bankOperations.add(BankTransaction.builder().accountNumber("TSG54SA").transactionDate(LocalDate.parse("2019-03-14", df).atStartOfDay())
            .beneficiary("Jane Doe")
            .amount(BigDecimal.TEN)
            .currency("USD")
            .build());
        bankOperations.add(BankTransaction.builder().accountNumber("JHADD54").transactionDate(LocalDate.parse("2004-03-14", df).atStartOfDay())
            .beneficiary("Joe Doe")
            .amount(BigDecimal.TEN)
            .currency("USD")
            .isWithdrawal(true)
            .build());

        createTestBankTransactionData(bankOperations);


        MockMultipartFile mockitoMultipartFile = new MockMultipartFile("test-data.csv", Files.newInputStream(Paths.get("/Users/evita/inventi-homework/src/test/java/com/inventi/homework/data/test-data.csv")));

        bankTransactionService.importCSV(mockitoMultipartFile);


        List<String> accountNumbers = new ArrayList<>();


        accountNumbers.add("TSG54SA");
        accountNumbers.add("JHADD54");

        BankTransactionStatementModel bm = BankTransactionStatementModel.builder().accountNumber(accountNumbers).dateFrom("2001-05-06").dateTo("2022-08-06").build();


        bankTransactionService.exportCSV(bm);
    }

    @Test
    void calculatesBalanceSuccessfully() throws IOException, ParseException {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<BankTransaction> bankOperations = new ArrayList<>();
        bankOperations.add(BankTransaction.builder().accountNumber("TSG54SA").transactionDate(LocalDate.parse("2004-03-14", df).atStartOfDay())
            .beneficiary("Jane Doe")
            .amount(BigDecimal.valueOf(20))
            .currency("EUR")
            .build());
        bankOperations.add(BankTransaction.builder().accountNumber("TSG54SA").transactionDate(LocalDate.parse("2019-03-14", df).atStartOfDay())
            .beneficiary("Jane Doe")
            .amount(BigDecimal.TEN)
            .isWithdrawal(true)
            .currency("USD")
            .build());

        createTestBankTransactionData(bankOperations);

        MockMultipartFile mockitoMultipartFile = new MockMultipartFile("test-data.csv", Files.newInputStream(Paths.get("/Users/evita/inventi-homework/src/test/java/com/inventi/homework/data/test-data.csv")));

        bankTransactionService.importCSV(mockitoMultipartFile);

        BigDecimal bd = bankTransactionService.calculateAccountBalance("TSG54SA", "2001-05-06", "2022-08-06");

        assertEquals(BigDecimal.TEN, bd);
    }

}