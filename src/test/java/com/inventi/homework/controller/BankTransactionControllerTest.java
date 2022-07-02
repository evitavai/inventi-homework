package com.inventi.homework.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventi.homework.entity.BankTransaction;
import com.inventi.homework.repository.BankAccountRepository;
import com.inventi.homework.repository.BankTransactionRepository;
import com.inventi.homework.service.BankTransactionService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.inventi.homework.helpers.TestHelpers.createTestBankAccounts;
import static com.inventi.homework.helpers.TestHelpers.createTestBankTransactionData;
import static com.inventi.homework.service.BankTransactionServiceTest.absoluteTestDataPath;
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
    private Environment env;
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMultipartFile mockMultipartFile;

    @BeforeEach
    void setup() throws IOException {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        FileUtils.deleteDirectory(new File(absoluteTestDataPath));
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "bank_account_transactions", "bank_accounts");//deletes all data from table before each test

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        ArrayList<String> bankAccountNumbers = new ArrayList<>();
        bankAccountNumbers.add("TSG54SA");
        bankAccountNumbers.add("JHADD54");

        createTestBankAccounts(bankAccountNumbers, bankAccountRepository);

        List<BankTransaction> bankTransactions = new ArrayList<>();
        bankTransactions.add(BankTransaction.builder().accountNumber("TSG54SA").transactionDate(LocalDateTime.now())
            .beneficiary("Jane Doe")
            .amount(BigDecimal.valueOf(50))
            .currency("EUR")
            .build());
        bankTransactions.add(BankTransaction.builder().accountNumber("JHADD54").transactionDate(LocalDate.parse("2019-03-14", dateTimeFormatter).atStartOfDay())
            .beneficiary("Joe Doe")
            .amount(BigDecimal.valueOf(20))
            .currency("USD")
            .isWithdrawal(true)
            .build());

        createTestBankTransactionData(bankTransactions, env);

        mockMultipartFile = new MockMultipartFile("file", Files.newInputStream(Paths.get(Objects.requireNonNull(env.getProperty("testDataFile.path"))).toAbsolutePath()));
    }

    @Test
    void importsBankStatementFromCsvFileSuccessfully() throws Exception {
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
        bankTransactionService.importCSV(mockMultipartFile);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/export")
                .param("accountNumbers", "TSG54SA")
                .param("accountNumbers", "JHADD54")
                .param("dateFrom", "2001-05-06")
                .param("dateTo", "2022-08-06")
            )
            .andExpect(status().is(200));

        InputStreamReader streamReader = new InputStreamReader(mockMultipartFile.getInputStream(), StandardCharsets.UTF_8);
        CsvToBean<BankTransaction> csvToBean = new CsvToBeanBuilder<BankTransaction>(streamReader)
            .withType(BankTransaction.class)
            .withSkipLines(1)
            .withIgnoreLeadingWhiteSpace(true)
            .build();

        List<BankTransaction> bankTransactionList = csvToBean.parse();

        assertEquals(2, bankTransactionList.size());
        assertEquals("TSG54SA", bankTransactionList.get(0).getAccountNumber());
        assertEquals("JHADD54", bankTransactionList.get(1).getAccountNumber());
    }

    @Test
    void calculatesBalanceSuccessfully() throws Exception {
        bankTransactionService.importCSV(mockMultipartFile);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/balance")
                .param("accountNumber", "TSG54SA")
                .param("dateFrom", "2001-05-06")
                .param("dateTo", "2022-08-06")
            )
            .andExpect(status().is(200))
            .andReturn();

        BigDecimal balance = objectMapper.readValue(result.getResponse().getContentAsString(),
            BigDecimal.class);

        assertEquals(BigDecimal.valueOf(50), balance);
    }

}