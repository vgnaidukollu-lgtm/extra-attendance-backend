package com.attendance.service;

import com.attendance.dto.AttendanceRequest;
import com.attendance.dto.AttendanceResponse;
import com.attendance.model.AttendanceEntry;
import com.attendance.model.User;
import com.attendance.repository.AttendanceEntryRepository;
import com.attendance.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceEntryRepository attendanceRepository;
    private final UserRepository userRepository;

    public AttendanceService(AttendanceEntryRepository attendanceRepository, UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AttendanceResponse createForStudent(String username, AttendanceRequest req) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        AttendanceEntry e = new AttendanceEntry();
        e.setUniversityId(req.universityId());
        e.setDate(req.date());
        e.setHours(req.hours());
        e.setReasonForAbsent(req.reasonForAbsent());
        e.setSubjectCode(req.subjectCode());
        e.setStudent(student);
        attendanceRepository.save(e);
        return toResponse(e, student.getUsername());
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> listForStudent(String username) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return attendanceRepository.findByStudentIdOrdered(student.getId()).stream()
                .map(e -> toResponse(e, e.getStudent().getUsername()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> listAll() {
        return attendanceRepository.findAllWithStudentOrdered().stream()
                .map(e -> toResponse(e, e.getStudent().getUsername()))
                .toList();
    }

    private static AttendanceResponse toResponse(AttendanceEntry e, String enteredBy) {
        return new AttendanceResponse(
                e.getsNo(),
                e.getUniversityId(),
                e.getDate(),
                e.getHours(),
                e.getReasonForAbsent(),
                e.getSubjectCode(),
                enteredBy
        );
    }
}
