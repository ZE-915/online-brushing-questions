package org.example.backend.util;

import jakarta.servlet.http.HttpServletRequest;

public final class UserContext {
    private UserContext() {
    }

    public static Long userId(HttpServletRequest request) {
        Object value = request.getAttribute("userId");
        return value == null ? null : (Long) value;
    }
}
