package com.inventi.homework.service;

import com.inventi.homework.BankAccountStatementBody;
import com.inventi.homework.entity.BankAccountOperation;
import com.inventi.homework.repository.BankAccountOperationRepository;
import com.inventi.homework.repository.BankAccountRepository;
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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.inventi.homework.helpers.TestHelpers.createTestBankAccounts;
import static com.inventi.homework.helpers.TestHelpers.createTestBankTransactionData;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class BankAccountOperationServiceTest {

    @Autowired
    private BankAccountOperationRepository bankAccountOperationRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BankAccountOperationService bankAccountOperationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void tearDown() throws IOException, ParseException {
        FileUtils.deleteDirectory(new File("/Users/evita/inventi-homework/src/test/java/com/inventi/homework/data/"));
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "bank_account_transactions", "bank_accounts");//deletes all data from table before each test

        ArrayList<String> bankAccountNumbers = new ArrayList<>();
        bankAccountNumbers.add("TSG54SA");
        bankAccountNumbers.add("JHADD54");

        createTestBankAccounts(bankAccountNumbers, bankAccountRepository);
    }

    @Test
    void importsBankStatementFromCsvFileSuccessfully() throws IOException {
        List<BankAccountOperation> bankOperations = new ArrayList<>();
        bankOperations.add(BankAccountOperation.builder().accountNumber("TSG54SA").operationDate(Timestamp.from(Instant.now()))
            .beneficiary("Jane Doe")
            .amount(BigDecimal.valueOf(20))
            .currency("EUR")
            .build());
        bankOperations.add(BankAccountOperation.builder().accountNumber("JHADD54").operationDate(Timestamp.from(Instant.now()))
            .beneficiary("Joe Doe")
            .amount(BigDecimal.TEN)
            .currency("USD")
            .isWithdrawal(true)
            .build());

        createTestBankTransactionData(bankOperations);

        MockMultipartFile mockitoMultipartFile = new MockMultipartFile("test", Files.newInputStream(Paths.get("/Users/evita/inventi-homework/src/test/java/com/inventi/homework/data/test-data.csv")));

        bankAccountOperationService.importCSV(mockitoMultipartFile);

        List<BankAccountOperation> importedTransactions = bankAccountOperationRepository.findAll();

        assertEquals(2, importedTransactions.size());
        assertEquals("TSG54SA", importedTransactions.get(0).getAccountNumber());
        assertEquals("JHADD54", importedTransactions.get(1).getAccountNumber());
    }

    @Test
    void exportsBankStatementToCsvFileSuccessfully() throws IOException, ParseException {
        DateFormat df = new SimpleDateFormat();
        ((SimpleDateFormat) df).applyPattern("yyyy-MM-dd");
        df.setLenient(false);
        List<BankAccountOperation> bankOperations = new ArrayList<>();
        bankOperations.add(BankAccountOperation.builder().accountNumber("TSG54SA").operationDate(new Timestamp(df.parse("2002-04-05").getTime()))
            .beneficiary("Jane Doe")
            .amount(BigDecimal.valueOf(20))
            .currency("EUR")
            .build());
        bankOperations.add(BankAccountOperation.builder().accountNumber("TSG54SA").operationDate(new Timestamp(df.parse("2019-08-05").getTime()))
            .beneficiary("Jane Doe")
            .amount(BigDecimal.TEN)
            .currency("USD")
            .build());
        bankOperations.add(BankAccountOperation.builder().accountNumber("JHADD54").operationDate(new Timestamp(df.parse("2004-04-05").getTime()))
            .beneficiary("Joe Doe")
            .amount(BigDecimal.TEN)
            .currency("USD")
            .isWithdrawal(true)
            .build());

        createTestBankTransactionData(bankOperations);


        MockMultipartFile mockitoMultipartFile = new MockMultipartFile("test", Files.newInputStream(Paths.get("/Users/evita/inventi-homework/src/test/java/com/inventi/homework/data/test-data.csv")));

        bankAccountOperationService.importCSV(mockitoMultipartFile);


        List<BankAccountStatementBody> forms = new ArrayList<>();


        forms.add(BankAccountStatementBody.builder().accountNumber("TSG54SA").dateFrom("2001-05-06").dateTo("2022-08-06").build());


        bankAccountOperationService.exportCSV(forms);
    }

    @Test
    void calculatesBalanceSuccessfully() throws IOException, ParseException {
        DateFormat df = new SimpleDateFormat();
        ((SimpleDateFormat) df).applyPattern("yyyy-MM-dd");
        df.setLenient(false);
        List<BankAccountOperation> bankOperations = new ArrayList<>();
        bankOperations.add(BankAccountOperation.builder().accountNumber("TSG54SA").operationDate(new Timestamp(df.parse("2002-04-05").getTime()))
            .beneficiary("Jane Doe")
            .amount(BigDecimal.valueOf(20))
            .currency("EUR")
            .build());
        bankOperations.add(BankAccountOperation.builder().accountNumber("TSG54SA").operationDate(new Timestamp(df.parse("2019-08-05").getTime()))
            .beneficiary("Jane Doe")
            .amount(BigDecimal.TEN)
            .isWithdrawal(true)
            .currency("USD")
            .build());

        createTestBankTransactionData(bankOperations);

        MockMultipartFile mockitoMultipartFile = new MockMultipartFile("test", Files.newInputStream(Paths.get("/Users/evita/inventi-homework/src/test/java/com/inventi/homework/data/test-data.csv")));

        bankAccountOperationService.importCSV(mockitoMultipartFile);

        BigDecimal bd = bankAccountOperationService.calculateAccountBalance("TSG54SA", "2001-05-06", "2022-08-06");

        assertEquals(BigDecimal.TEN, bd);
    }

}