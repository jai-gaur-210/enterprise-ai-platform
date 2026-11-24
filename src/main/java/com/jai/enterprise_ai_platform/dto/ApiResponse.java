package com.jai.enterprise_ai_platform.dto;

import java.time.Instant;

public record ApiResponse<T>(Instant timestamp, String message, T data) {
}