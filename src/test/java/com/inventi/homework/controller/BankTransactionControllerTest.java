package com.inventi.homework.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventi.homework.entity.BankTransaction;
import com.inventi.homework.model.BankTransactionStatementModel;
import com.inventi.homework.repository.BankAccountRepository;
import com.inventi.homework.repository.BankTransactionRepository;
import com.inventi.homework.service.BankTransactionService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.inventi.homework.helpers.TestHelpers.createTestBankAccounts;
import static com.inventi.homework.helpers.TestHelpers.createTestBankTransactionData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class BankTransactionControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private BankTransactionRepository bankTransactionRepository;
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private BankTransactionService bankTransactionService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMultipartFile mockMultipartFile;


    @BeforeEach
    void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File("/Users/evita/inventi-homework/src/test/java/com/inventi/homework/data/"));
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "bank_account_transactions", "bank_accounts");//deletes all data from table before each test

        ArrayList<String> bankAccountNumbers = new ArrayList<>();
        bankAccountNumbers.add("TSG54SA");
        bankAccountNumbers.add("JHADD54");

        createTestBankAccounts(bankAccountNumbers, bankAccountRepository);

        List<BankTransaction> bankOperations = new ArrayList<>();
        bankOperations.add(BankTransaction.builder().accountNumber("TSG54SA").transactionDate(LocalDateTime.now())
            .beneficiary("Jane Doe")
            .amount(BigDecimal.valueOf(50))
            .currency("EUR")
            .build());
        bankOperations.add(BankTransaction.builder().accountNumber("JHADD54").transactionDate(LocalDateTime.now())
            .beneficiary("Joe Doe")
            .amount(BigDecimal.valueOf(20))
            .currency("USD")
            .isWithdrawal(true)
            .build());

        createTestBankTransactionData(bankOperations);

        mockMultipartFile = new MockMultipartFile("file", Files.newInputStream(Paths.get("/Users/evita/inventi-homework/src/test/java/com/inventi/homework/data/test-data.csv")));

    }

    @Test
    void importsBankStatementFromCsvFileSuccessfully() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/import")
                .file(mockMultipartFile))
            .andExpect(status().is(200));

        List<BankTransaction> importedTransactions = bankTransactionRepository.findAll();

        assertEquals(2, importedTransactions.size());
        assertEquals("TSG54SA", importedTransactions.get(0).getAccountNumber());
        assertEquals("JHADD54", importedTransactions.get(1).getAccountNumber());
    }

    @Test
    void exportsBankStatementToCsvFileSuccessfully() throws Exception {
        List<String> accountNumbers = new ArrayList<>();

        accountNumbers.add("TSG54SA");
        accountNumbers.add("JHADD54");

        BankTransactionStatementModel bm = BankTransactionStatementModel.builder().accountNumber(accountNumbers).dateFrom("2001-05-06").dateTo("2022-08-06").build();

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/export")
                    .sessionAttr("bankAccountStatement", bm)
//                .param("accountNumbers", String.valueOf(accountNumbers))
//                .param("dateFrom", "2001-05-06")
//                .param("dateTo", "2022-08-06")
            )
            .andExpect(status().is(200));

//        List<BankTransaction> importedTransactions = bankTransactionRepository.findAll();
//
//
//        assertEquals(2, importedTransactions.size());
//        assertEquals("TSG54SA", importedTransactions.get(0).getAccountNumber());
//        assertEquals("JHADD54", importedTransactions.get(1).getAccountNumber());
    }
//
//    @Test
//    void calculatesBalanceSuccessfully() throws IOException {
//
//        MockMultipartFile mockitoMultipartFile = new MockMultipartFile("test", Files.newInputStream(Paths.get("/Users/evita/inventi-homework/test-data.csv")));
//
//        bankAccountOperationService.importCSV(mockitoMultipartFile);
//
//        List<BankAccountOperation> importedTransactions = bankAccountOperationRepository.findAll();
//
//        assertEquals(2, importedTransactions.size());
//        assertEquals("TSG54SA", importedTransactions.get(0).getAccountNumber());
//        assertEquals("JHADD54", importedTransactions.get(1).getAccountNumber());
//    }

}