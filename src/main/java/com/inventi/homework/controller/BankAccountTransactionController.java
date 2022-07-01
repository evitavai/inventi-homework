package com.inventi.homework.controller;

import com.inventi.homework.BankAccountStatementForm;
import com.inventi.homework.service.BankAccountOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BankAccountTransactionController {
    private final BankAccountOperationService bankAccountOperationService;

    @PostMapping("/import")
    public void importCsvFile(@RequestParam("file") MultipartFile file) throws Exception {
        bankAccountOperationService.importCSV(file);
    }

    @GetMapping("/export")
    public void exportCSVFile(
        @RequestBody List<BankAccountStatementForm> bankAccountStatementForm) {
        bankAccountOperationService.exportCSV(bankAccountStatementForm);
    }

//    @GetMapping("/calculate-balance")
//    public double calculateAccountBalance(@RequestParam String accountNumber,
//                                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFrom,
//                                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateTo) {
//        return bankStatementsService.calculateAccountBalance(accountNumber, dateFrom, dateTo);
//    }
}
