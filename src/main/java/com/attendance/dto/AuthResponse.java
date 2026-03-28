package com.attendance.dto;

import com.attendance.model.Role;

public record AuthResponse(String token, String username, Role role) {}
