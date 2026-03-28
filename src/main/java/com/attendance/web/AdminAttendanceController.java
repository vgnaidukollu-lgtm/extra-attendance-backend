package com.attendance.web;

import com.attendance.dto.AttendanceResponse;
import com.attendance.service.AttendanceService;
import com.attendance.service.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/attendance")
public class AdminAttendanceController {

    private final AttendanceService attendanceService;
    private final ExportService exportService;

    public AdminAttendanceController(AttendanceService attendanceService, ExportService exportService) {
        this.attendanceService = attendanceService;
        this.exportService = exportService;
    }

    @GetMapping
    public ResponseEntity<List<AttendanceResponse>> all() {
        return ResponseEntity.ok(attendanceService.listAll());
    }

    @GetMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv() {
        byte[] body = exportService.toCsv(attendanceService.listAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(body);
    }

    @GetMapping(value = "/export/excel")
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        byte[] body = exportService.toExcel(attendanceService.listAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(body);
    }
}
