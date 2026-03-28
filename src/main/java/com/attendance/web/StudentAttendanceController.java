package com.attendance.web;

import com.attendance.dto.AttendanceRequest;
import com.attendance.dto.AttendanceResponse;
import com.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student/attendance")
public class StudentAttendanceController {

    private final AttendanceService attendanceService;

    public StudentAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    public ResponseEntity<AttendanceResponse> create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody AttendanceRequest req
    ) {
        return ResponseEntity.ok(attendanceService.createForStudent(principal.getUsername(), req));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<AttendanceResponse>> mine(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(attendanceService.listForStudent(principal.getUsername()));
    }
}
