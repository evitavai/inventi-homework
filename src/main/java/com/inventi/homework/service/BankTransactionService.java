package com.inventi.homework.service;

import com.inventi.homework.controller.requestdto.BankTransactionStatementParams;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.ParseException;

public interface BankTransactionService {

    void importCSV(MultipartFile file);

    void exportCSV(BankTransactionStatementParams bankAccountStatementForm, Pageable pageable);

    BigDecimal calculateAccountBalance(String accountNumber, String dateFrom, String dateTo, Pageable pageable) throws ParseException;
}
