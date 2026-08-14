package com.mk.www.smsmonitor.transaction.infrastructure.exporter;

import com.mk.www.smsmonitor.transaction.domain.Transaction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test")
public class GoogleSheetExporterIntegrationTest {

    @Autowired
    private GoogleSheetExporter googleSheetExporter;

    @Test
    void testActualExport() {
        Transaction transaction = Transaction.builder()
                .vendor("테스트 점포")
                .amount(new BigDecimal("10000"))
                .transactionTime(LocalDateTime.now())
                .memo("AI 자동 테스트")
                .build();

        System.out.println(">>> Starting Google Sheet Export Test...");
        googleSheetExporter.export(transaction);
        System.out.println(">>> Export attempt finished. Check logs above for success/failure.");
    }
}
