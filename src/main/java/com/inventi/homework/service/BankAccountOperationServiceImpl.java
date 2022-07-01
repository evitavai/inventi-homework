package com.inventi.homework.service;

import com.inventi.homework.BankAccountStatementForm;
import com.inventi.homework.entity.BankAccountOperation;
import com.inventi.homework.repository.BankAccountOperationRepository;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
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

            bankAccountOperationRepository.saveAll(b);

        } catch (IOException e) {
//            log.error("Can't convert file to target class: {}", target.getSimpleName(), e);
//            throw new TppException("Can't convert file to target class", 400);
        }

    }

    @Override
    public void exportCSV(List<BankAccountStatementForm> bankAccountStatementForm) {
        String filename = "bankStatement.csv";

        bankAccountStatementForm.forEach((st) -> {
            try {
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                Timestamp formattedDateFrom = new Timestamp(dateFormatter.parse(String.valueOf(st.getDateFrom())).getTime());
                Timestamp formattedDateTo = null;
                formattedDateTo = new Timestamp(dateFormatter.parse(String.valueOf(st.getDateTo())).getTime());

                List<BankAccountOperation> bankOperations = bankAccountOperationRepository.findByAccountBalancesBetween(st.getAccountNumber(), formattedDateFrom, formattedDateTo);

                StringWriter sw = new StringWriter();
                CSVWriter writer = new CSVWriter(new FileWriter(filename));

                //Write header
                String[] header = {"account_number", "operation_date", "beneficiary", "comment", "amount", "currency"};
                writer.writeNext(header);

                //Write data
                String[] data;
                for (BankAccountOperation s : bankOperations) {
                    data = new String[]{String.valueOf(s.getAccountNumber()), String.valueOf(s.getOperationDate()), s.getBeneficiary(), s.getComment(), String.valueOf(s.getAmount()), s.getCurrency()};
                    writer.writeNext(data);
                }
                writer.close();
            } catch (IOException | ParseException e) {
//            log.error("Can't convert file to target class: {}", target.getSimpleName(), e);
//            throw new TppException("Can't convert file to target class", 400);
            }
        });
    }

    @Override
    public double calculateAccountBalance(String accountNumber, Date dateFrom, Date dateTo) {
        handleEmptyDateParams(dateFrom, dateTo);
        return ApplicationUtils.getCsvModelList().stream()
            .filter(csvModel -> csvModel.getAccountNumber().equals(accountNumber))
            .mapToDouble(csvModel -> csvModel.getAmount()).sum();
    }

}
