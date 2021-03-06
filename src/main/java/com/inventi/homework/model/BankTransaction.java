package com.inventi.homework.model;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "bank_account_transactions")
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @CsvBindByPosition(position = 0, required = true)
    private String accountNumber;

    @NotNull
    @Column(name = "transaction_date")
    @CsvBindByPosition(position = 1, required = true)
    @CsvDate(value = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionDate;

    @NotNull
    @Column(name = "beneficiary")
    @CsvBindByPosition(position = 2, required = true)
    private String beneficiary;

    @Column(name = "comment")
    @CsvBindByPosition(position = 3)
    private String comment;

    @NotNull
    @Column(name = "amount")
    @CsvBindByPosition(position = 4, required = true)
    private BigDecimal amount;

    @NotNull
    @Column(name = "currency")
    @CsvBindByPosition(position = 5, required = true)
    private String currency;

    @Column(name = "is_withdrawal")
    @CsvBindByPosition(position = 6)
    private boolean isWithdrawal;
}
