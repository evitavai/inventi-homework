package com.inventi.homework.model;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
public class BankAccountOperationCsvModel {

    @CsvBindByPosition(position = 0, required = true)
    private String accountNumber;

    @CsvBindByPosition(position = 1, required = true)
    @CsvDate(value = "yyyy-MM-dd HH:mm:ss")
    private Timestamp operationDate;

    @CsvBindByPosition(position = 2, required = true)
    private String beneficiary;

    @CsvBindByPosition(position = 3)
    private String comment;

    @CsvBindByPosition(position = 4, required = true)
    private BigDecimal amount;

    @CsvBindByPosition(position = 5, required = true)
    private String currency;
}
