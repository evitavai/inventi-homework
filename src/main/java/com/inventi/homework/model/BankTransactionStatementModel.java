package com.inventi.homework.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankTransactionStatementModel {
    @NonNull
    private List<String> accountNumber;

    private String dateFrom;

    private String dateTo;
}
