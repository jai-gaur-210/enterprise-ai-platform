package com.jai.enterprise_ai_platform.dto;

import java.util.UUID;

public record UserResponse(UUID id, String name, String email) {
}
