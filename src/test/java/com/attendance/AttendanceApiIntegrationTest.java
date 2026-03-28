package com.attendance;

import com.attendance.model.Role;
import com.attendance.model.User;
import com.attendance.repository.AttendanceEntryRepository;
import com.attendance.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AttendanceApiIntegrationTest {

    @DynamicPropertySource
    static void h2Properties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> "jdbc:h2:mem:attendance_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        r.add("spring.datasource.username", () -> "sa");
        r.add("spring.datasource.password", () -> "");
        r.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.H2Dialect");
    }

    @Autowired
    TestRestTemplate rest;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AttendanceEntryRepository attendanceEntryRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void resetUsers() {
        attendanceEntryRepository.deleteAll();
        userRepository.deleteAll();
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("adminpass"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
    }

    @Test
    void studentRegistersCreatesEntryAdminSeesAll() throws Exception {
        HttpHeaders json = new HttpHeaders();
        json.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> reg = rest.exchange(
                "/api/auth/register",
                HttpMethod.POST,
                new HttpEntity<>("{\"username\":\"student1\",\"password\":\"studentpass\"}", json),
                String.class
        );
        assertThat(reg.getStatusCode().is2xxSuccessful()).isTrue();
        String studentToken = objectMapper.readTree(reg.getBody()).get("token").asText();

        HttpHeaders studentAuth = new HttpHeaders();
        studentAuth.setContentType(MediaType.APPLICATION_JSON);
        studentAuth.setBearerAuth(studentToken);

        String entryJson = "{\"universityId\":\"U001\",\"date\":\"2026-03-28\",\"hours\":2,\"reasonForAbsent\":\"Medical\",\"subjectCode\":\"CS101\"}";
        ResponseEntity<String> create = rest.exchange(
                "/api/student/attendance",
                HttpMethod.POST,
                new HttpEntity<>(entryJson, studentAuth),
                String.class
        );
        assertThat(create.getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<String> mine = rest.exchange(
                "/api/student/attendance/mine",
                HttpMethod.GET,
                new HttpEntity<>(null, studentAuth),
                String.class
        );
        assertThat(mine.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(mine.getBody()).contains("U001", "CS101");

        HttpHeaders adminAuth = new HttpHeaders();
        adminAuth.setBearerAuth(loginAdmin());

        ResponseEntity<String> all = rest.exchange(
                "/api/admin/attendance",
                HttpMethod.GET,
                new HttpEntity<>(null, adminAuth),
                String.class
        );
        assertThat(all.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(all.getBody()).contains("U001", "student1");
    }

    @Test
    void studentCannotAccessAdminApi() throws Exception {
        HttpHeaders json = new HttpHeaders();
        json.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> reg = rest.exchange(
                "/api/auth/register",
                HttpMethod.POST,
                new HttpEntity<>("{\"username\":\"stu2\",\"password\":\"studentpass\"}", json),
                String.class
        );
        String token = objectMapper.readTree(reg.getBody()).get("token").asText();
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);

        ResponseEntity<String> blocked = rest.exchange(
                "/api/admin/attendance",
                HttpMethod.GET,
                new HttpEntity<>(null, h),
                String.class
        );
        assertThat(blocked.getStatusCode().value()).isEqualTo(403);
    }

    private String loginAdmin() throws Exception {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> res = rest.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>("{\"username\":\"admin\",\"password\":\"adminpass\"}", h),
                String.class
        );
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        return objectMapper.readTree(res.getBody()).get("token").asText();
    }
}
