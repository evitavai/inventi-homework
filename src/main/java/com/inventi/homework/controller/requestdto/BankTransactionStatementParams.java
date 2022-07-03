package com.inventi.homework.controller.requestdto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankTransactionStatementParams {
    @NonNull
    private List<String> accountNumber;

    private String dateFrom;

    private String dateTo;
}
