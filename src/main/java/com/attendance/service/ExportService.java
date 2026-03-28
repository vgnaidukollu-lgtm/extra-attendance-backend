package com.attendance.service;

import com.attendance.dto.AttendanceResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ExportService {

    public byte[] toCsv(List<AttendanceResponse> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("S.No.,University ID,Date,Hours,Reason for absent,Subject code,Entered by\n");
        for (AttendanceResponse r : rows) {
            sb.append(r.sNo()).append(',')
                    .append(escapeCsv(r.universityId())).append(',')
                    .append(r.date()).append(',')
                    .append(r.hours()).append(',')
                    .append(escapeCsv(r.reasonForAbsent())).append(',')
                    .append(escapeCsv(r.subjectCode())).append(',')
                    .append(escapeCsv(r.enteredByUsername()))
                    .append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String escapeCsv(String s) {
        if (s == null) {
            return "";
        }
        boolean needQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String t = s.replace("\"", "\"\"");
        return needQuotes ? "\"" + t + "\"" : t;
    }

    public byte[] toExcel(List<AttendanceResponse> rows) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Attendance");
            Row header = sheet.createRow(0);
            String[] cols = {"S.No.", "University ID", "Date", "Hours", "Reason for absent", "Subject code", "Entered by"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
            int r = 1;
            for (AttendanceResponse row : rows) {
                Row excelRow = sheet.createRow(r++);
                excelRow.createCell(0).setCellValue(row.sNo() != null ? row.sNo() : 0);
                excelRow.createCell(1).setCellValue(row.universityId());
                excelRow.createCell(2).setCellValue(row.date() != null ? row.date().toString() : "");
                excelRow.createCell(3).setCellValue(row.hours() != null ? row.hours() : 0);
                excelRow.createCell(4).setCellValue(row.reasonForAbsent());
                excelRow.createCell(5).setCellValue(row.subjectCode());
                excelRow.createCell(6).setCellValue(row.enteredByUsername());
            }
            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        }
    }
}
