package com.verify.panverification.service;

import com.verify.panverification.entity.PanVerification;
import com.verify.panverification.repository.PanVerificationRepository;
import com.verify.panverification.util.ExcelGenerator;
import com.verify.panverification.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PanVerificationRepository repository;
    private final PdfGenerator pdfGenerator;
    private final ExcelGenerator excelGenerator;

    public ByteArrayInputStream generatePdf() throws Exception {

        List<PanVerification> data =
                repository.findAll();

        return pdfGenerator.generate(data);
    }

    public ByteArrayInputStream generateExcel()
            throws Exception {

        return excelGenerator.generate(
                repository.findAll()
        );
    }
}
