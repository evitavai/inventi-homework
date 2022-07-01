package com.inventi.homework;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountStatementForm {
    private String accountNumber;
    private Date dateFrom;
    private Date dateTo;
}
