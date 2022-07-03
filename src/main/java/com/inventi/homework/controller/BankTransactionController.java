package com.inventi.homework.controller;

import com.inventi.homework.controller.requestdto.BankTransactionStatementParams;
import com.inventi.homework.service.BankTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class BankTransactionController {
    private final BankTransactionService bankTransactionService;

    @PostMapping("/import")
    public ResponseEntity<String> importCsvFile(@RequestPart("file") MultipartFile file) {
        try {
            bankTransactionService.importCSV(file);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            log.error("Exact bank transaction record already exists");
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Error while importing CSV file", e);
        }
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportCSVFile(
        @RequestParam(defaultValue = "0") Integer pageNumber,
        @RequestParam(defaultValue = "5") Integer pageSize,
        @RequestParam @Valid List<String> accountNumbers,
        //ran into an issue referenced here -> https://github.com/spring-projects/spring-data-jpa/issues/2491 , used one of the solutions mentioned. Same for the endpoint below
        @RequestParam(defaultValue = "1000-01-01", required = false) String dateFrom,
        @RequestParam(defaultValue = "9999-01-01", required = false) String dateTo) {
        try {
            BankTransactionStatementParams bankTransactionStatementParams = BankTransactionStatementParams.builder().accountNumber(accountNumbers).dateFrom(dateFrom).dateTo(dateTo).build();
            bankTransactionService.exportCSV(bankTransactionStatementParams, PageRequest.of(pageNumber, pageSize));
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Error while exporting CSV file", e);
        }
    }

    @GetMapping("/balance")
    public @ResponseBody BigDecimal calculateAccountBalance(@RequestParam @Valid String accountNumber,
                                                            @RequestParam(defaultValue = "1000-01-01", required = false) String dateFrom,
                                                            @RequestParam(defaultValue = "9999-01-01", required = false) String dateTo,
                                                            @RequestParam(defaultValue = "0") Integer pageNumber,
                                                            @RequestParam(defaultValue = "5") Integer pageSize) {
        try {
            return bankTransactionService.calculateAccountBalance(accountNumber, dateFrom, dateTo, PageRequest.of(pageNumber, pageSize));
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Error while calculating account balance", e);
        }
    }
}
