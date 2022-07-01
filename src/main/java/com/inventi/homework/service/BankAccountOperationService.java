package com.inventi.homework.service;

import com.inventi.homework.BankAccountStatementForm;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface BankAccountOperationService {

     void importCSV(MultipartFile file) throws IOException;

     void exportCSV(List<BankAccountStatementForm> bankAccountStatementForm);

     BigDecimal calculateAccountBalance(String accountNumber, Date dateFrom, Date dateTo);
}
