package com.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AttendanceRequest(
        @NotBlank @Size(max = 64) String universityId,
        @NotNull LocalDate date,
        @NotNull @Positive Integer hours,
        @NotBlank @Size(max = 512) String reasonForAbsent,
        @NotBlank @Size(max = 32) String subjectCode
) {}
