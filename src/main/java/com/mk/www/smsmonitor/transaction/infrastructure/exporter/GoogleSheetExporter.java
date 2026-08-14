package com.mk.www.smsmonitor.transaction.infrastructure.exporter;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.mk.www.smsmonitor.transaction.application.DataExporter;
import com.mk.www.smsmonitor.transaction.domain.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class GoogleSheetExporter implements DataExporter {

    @Value("${google.credentials.path:}")
    private String credentialsPath;
    
    @Value("${google.sheets.spreadsheet-id:}")
    private String spreadsheetId;

    @Value("${google.sheets.sheet-name:sheet1}")
    private String sheetName;

    private Sheets getSheetsService() throws IOException, GeneralSecurityException {
        log.info("Loading Google Credentials from path: {}", credentialsPath);
        
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new FileInputStream(credentialsPath))
                .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets"));

        if (credentials instanceof com.google.auth.oauth2.ServiceAccountCredentials) {
            String clientEmail = ((com.google.auth.oauth2.ServiceAccountCredentials) credentials).getClientEmail();
            log.info("Loaded Service Account Email: [{}]", clientEmail);
        }

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName("SmsMonitor").build();
    }

    @Override
    public void export(Transaction transaction) {
        if (credentialsPath == null || credentialsPath.isBlank() || spreadsheetId == null || spreadsheetId.isBlank()) {
            log.warn("Google Sheet export skipped: credentials path or spreadsheet ID is not configured.");
            return;
        }

        java.io.File file = new java.io.File(credentialsPath);
        if (!file.exists()) {
            log.warn("Google Sheet export skipped: credentials file does not exist at: {}", credentialsPath);
            return;
        }

        try {
            Sheets sheetsService = getSheetsService();

            String categoryName = transaction.getCategory() != null ? transaction.getCategory() : "";

            List<Object> rowData = List.of(
                    transaction.getTransactionTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    transaction.getVendor() == null ? "" : transaction.getVendor(),
                    transaction.getAmount() == null ? "" : transaction.getAmount(),
                    categoryName,
                    transaction.getMemo() == null ? "" : transaction.getMemo()
            );

            ValueRange body = new ValueRange().setValues(List.of(rowData));

            AppendValuesResponse result = sheetsService.spreadsheets().values()
                    .append(spreadsheetId, sheetName, body)
                    .setValueInputOption("USER_ENTERED")
                    .setInsertDataOption("INSERT_ROWS")
                    .execute();

            log.error("Appended data to Google Sheet: " + result);

        } catch (IOException | GeneralSecurityException e) {
            log.error("Error exporting transaction to Google Sheet: " + e.getMessage());
        }
    }
}
