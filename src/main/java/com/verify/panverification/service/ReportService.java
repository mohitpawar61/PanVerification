package com.verify.panverification.service;

import com.verify.panverification.entity.PanVerification;
import com.verify.panverification.repository.PanVerificationRepository;
import com.verify.panverification.util.ExcelGenerator;
import com.verify.panverification.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final PanVerificationRepository repository;
    private final PdfGenerator pdfGenerator;
    private final ExcelGenerator excelGenerator;

    public ByteArrayInputStream generatePdf() throws Exception {

        log.info("Generating pdf report");
        List<PanVerification> data =repository.findAll();
                log.debug("Fetched {} records for PDF report",data.size());

                ByteArrayInputStream pdf = pdfGenerator.generate(data);
                log.info("PDF report generated successfully with {} records",data.size());

        return pdf;
    }

    public ByteArrayInputStream generateExcel()
            throws Exception {

        log.info("Generating excel report");
        List<PanVerification> data = repository.findAll();

        log.debug("Fetched {} records for Excel report",data.size());

        ByteArrayInputStream excel = excelGenerator.generate(data);
        log.info("Excel report generated successfully with {} records",data.size());

        return excel;


    }
}
