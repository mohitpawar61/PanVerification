package com.verify.panverification.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.verify.panverification.entity.PanVerification;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Component
public class PdfGenerator {

    public ByteArrayInputStream generate(
            List<PanVerification> data)
            throws Exception {

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

        for(PanVerification p : data){

            document.add(
                    new Paragraph(
                            p.getPanNumber()
                    )
            );
        }

        document.close();

        return new ByteArrayInputStream(
                out.toByteArray()
        );
    }
}
