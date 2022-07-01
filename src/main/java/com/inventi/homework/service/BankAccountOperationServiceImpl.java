package com.inventi.homework.service;

import com.inventi.homework.BankAccountStatementBody;
import com.inventi.homework.entity.BankAccountOperation;
import com.inventi.homework.repository.BankAccountOperationRepository;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountOperationServiceImpl implements BankAccountOperationService {

    private final BankAccountOperationRepository bankAccountOperationRepository;

    @Override
    public void importCSV(MultipartFile file) throws IOException {
//        if (file == null) {
//            throw new TppException("No file uploaded!", 400);
//        }
        try {
            InputStreamReader streamReader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
            CsvToBean<BankAccountOperation> accountBalances = new CsvToBeanBuilder<BankAccountOperation>(streamReader)
                .withType(BankAccountOperation.class)
                .withSkipLines(1)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

            List<BankAccountOperation> b = accountBalances.parse();

            b.forEach((u) -> {
                if (u.isWithdrawal()) {
                    u.setAmount(u.getAmount().negate());
                }
            });


            bankAccountOperationRepository.saveAll(b);

        } catch (IOException e) {
//            log.error("Can't convert file to target class: {}", target.getSimpleName(), e);
//            throw new TppException("Can't convert file to target class", 400);
        }

    }

    @Override
    public void exportCSV(List<BankAccountStatementBody> bankAccountStatementForm) {
        bankAccountStatementForm.forEach((st) -> {
            try {
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                Timestamp formattedDateFrom = new Timestamp(dateFormatter.parse(st.getDateFrom()).getTime());
                Timestamp formattedDateTo = new Timestamp(dateFormatter.parse(st.getDateTo()).getTime());

                List<BankAccountOperation> bankOperations = bankAccountOperationRepository.findByAccountBalancesBetween(st.getAccountNumber(), formattedDateFrom, formattedDateTo);

                File csvOutputFile = new File("/Users/evita/inventi-homework/src/test/java/com/inventi/homework/data/bank-statement-output.csv");
                csvOutputFile.getParentFile().mkdirs();

                CSVWriter writer = new CSVWriter(new FileWriter(csvOutputFile));

                //Write header
                String[] header = {"account_number", "operation_date", "beneficiary", "comment", "amount", "currency", "is_withdrawal"};
                writer.writeNext(header);

                //Write data
                String[] data;
                for (BankAccountOperation s : bankOperations) {
                    data = new String[]{String.valueOf(s.getAccountNumber()), String.valueOf(s.getOperationDate()), s.getBeneficiary(), s.getComment(), String.valueOf(s.getAmount()), s.getCurrency(), String.valueOf(s.isWithdrawal())};
                    writer.writeNext(data);
                }
                writer.close();
            } catch (IOException | ParseException e) {
                System.out.println(e);
//            log.error("Can't convert file to target class: {}", target.getSimpleName(), e);
//            throw new TppException("Can't convert file to target class", 400);
            }
        });
    }

    @Override
    public BigDecimal calculateAccountBalance(String accountNumber, String dateFrom, String dateTo) throws ParseException {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Timestamp formattedDateFrom = new Timestamp(dateFormatter.parse(dateFrom).getTime());
        Timestamp formattedDateTo = new Timestamp(dateFormatter.parse(dateTo).getTime());

        List<BankAccountOperation> bankOperations = bankAccountOperationRepository.findByAccountBalancesBetween(accountNumber, formattedDateFrom, formattedDateTo);

        BigDecimal sum = BigDecimal.ZERO;

//        for (int i = 0; i < bankOperations.size(); i++)
//        {
//            sum = sum.add(new BigDecimal(String.valueOf(bankOperations.get(i))));
//        }

        for (BankAccountOperation amt : bankOperations) {
            sum = sum.add(amt.getAmount());
        }

        return sum;

    }

}
