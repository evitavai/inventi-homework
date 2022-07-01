package com.inventi.homework;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountStatementBody {
    private String accountNumber;
    private String dateFrom;
    private String dateTo;
}
