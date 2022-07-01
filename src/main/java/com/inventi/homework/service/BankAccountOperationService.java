package com.inventi.homework.service;

import com.inventi.homework.BankAccountStatementBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

public interface BankAccountOperationService {

     void importCSV(MultipartFile file) throws IOException;

     void exportCSV(List<BankAccountStatementBody> bankAccountStatementForm);

     BigDecimal calculateAccountBalance(String accountNumber, String dateFrom, String dateTo) throws ParseException;
}
