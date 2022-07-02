package com.inventi.homework.controller;

import com.inventi.homework.model.BankTransactionStatementModel;
import com.inventi.homework.service.BankTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BankTransactionController {
    private final BankTransactionService bankTransactionService;

    @PostMapping("/import")
    public ResponseEntity<String> importCsvFile(@RequestPart("file") MultipartFile file) throws Exception {
        bankTransactionService.importCSV(file);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportCSVFile(
        @RequestParam @Valid List<String> accountNumbers,
        @RequestParam(required = false) String dateFrom,
        @RequestParam(required = false) String dateTo) {
        BankTransactionStatementModel bm = BankTransactionStatementModel.builder().accountNumber(accountNumbers).dateFrom(dateFrom).dateTo(dateTo).build();
        bankTransactionService.exportCSV(bm);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/balance")
    public @ResponseBody BigDecimal calculateAccountBalance(@RequestParam @Valid String accountNumber,
                                                            @RequestParam(required = false) String dateFrom,
                                                            @RequestParam(required = false) String dateTo) throws ParseException {
        return bankTransactionService.calculateAccountBalance(accountNumber, dateFrom, dateTo);
    }
}
