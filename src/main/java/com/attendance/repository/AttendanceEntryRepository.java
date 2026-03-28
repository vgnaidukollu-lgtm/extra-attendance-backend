package com.attendance.repository;

import com.attendance.model.AttendanceEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AttendanceEntryRepository extends JpaRepository<AttendanceEntry, Long> {

    @Query("SELECT e FROM AttendanceEntry e WHERE e.student.id = :studentId ORDER BY e.date DESC, e.sNo DESC")
    List<AttendanceEntry> findByStudentIdOrdered(@Param("studentId") Long studentId);

    @Query("SELECT e FROM AttendanceEntry e JOIN FETCH e.student ORDER BY e.date DESC, e.sNo DESC")
    List<AttendanceEntry> findAllWithStudentOrdered();
}
