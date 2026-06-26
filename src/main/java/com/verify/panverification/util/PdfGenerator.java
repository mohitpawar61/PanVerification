package com.verify.panverification.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.verify.panverification.entity.PanVerification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
@Component
public class PdfGenerator {

    public ByteArrayInputStream generate(
            List<PanVerification> data)
            throws Exception {

        log.info("Starting PDF generation for {} records",data.size());

        Document document =
                new Document();

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        PdfWriter.getInstance(
                document,
                out
        );

        document.open();

        document.add(
                new Paragraph(
                        "PAN Verification Report"
                )
        );

        log.debug("PDF header added");

        for(PanVerification p : data){

            document.add(
                    new Paragraph(
                            p.getPanNumber()
                    )
            );
        }

        document.close();
        log.info("PDF generation completed successfully");

        return new ByteArrayInputStream(
                out.toByteArray()
        );
    }
}
