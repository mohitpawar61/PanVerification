package com.verify.panverification.util;

import com.verify.panverification.entity.PanVerification;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Component
public class ExcelGenerator {

    public ByteArrayInputStream generate(
            List<PanVerification> data)
            throws Exception {

        Workbook workbook =
                new XSSFWorkbook();

        Sheet sheet =
                workbook.createSheet(
                        "Verifications"
                );

        int rowNum = 0;

        for(PanVerification p : data){

            Row row =
                    sheet.createRow(
                            rowNum++
                    );

            row.createCell(0)
                    .setCellValue(
                            p.getPanNumber()
                    );

            row.createCell(1)
                    .setCellValue(
                            p.getPanStatus()
                    );
        }

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        workbook.write(out);

        workbook.close();

        return new ByteArrayInputStream(
                out.toByteArray()
        );
    }
}
