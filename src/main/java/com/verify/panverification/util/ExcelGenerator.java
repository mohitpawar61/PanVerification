package com.verify.panverification.util;

import com.verify.panverification.entity.PanVerification;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
@Component
public class ExcelGenerator {

    public ByteArrayInputStream generate(
            List<PanVerification> data)
            throws Exception {

        log.info("Starting Excel generation for {} records",data.size());
        Workbook workbook =
                new XSSFWorkbook();

        Sheet sheet =
                workbook.createSheet(
                        "Verifications"
                );
        log.debug("Excel sheet 'Verification' created");

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

            log.debug("Added row {} for PAN: {}",rowNum,p.getPanNumber());
        }

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        workbook.write(out);

        workbook.close();

        log.info("Excel generation completed successfully with {} rows",rowNum);

        return new ByteArrayInputStream(
                out.toByteArray()
        );
    }
}
