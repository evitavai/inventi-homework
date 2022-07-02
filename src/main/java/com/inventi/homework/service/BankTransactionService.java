package com.inventi.homework.service;

import com.inventi.homework.model.BankTransactionStatementModel;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;

public interface BankTransactionService {

    void importCSV(MultipartFile file) throws IOException;

    void exportCSV(BankTransactionStatementModel bankAccountStatementForm);

    BigDecimal calculateAccountBalance(String accountNumber, String dateFrom, String dateTo) throws ParseException;
}
