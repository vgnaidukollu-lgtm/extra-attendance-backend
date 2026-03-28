package com.attendance.dto;

import java.time.LocalDate;

public record AttendanceResponse(
        Long sNo,
        String universityId,
        LocalDate date,
        Integer hours,
        String reasonForAbsent,
        String subjectCode,
        String enteredByUsername
) {}
