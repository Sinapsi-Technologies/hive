package com.example.todo.domain.valueobjects;

/** Value object: a non-blank todo title, trimmed and length-bounded. */
public record TodoTitle(String value) {
    public static final int MAX_LENGTH = 140;

    public TodoTitle {
        if (value == null) {
            throw new IllegalArgumentException("Todo title is required");
        }
        value = value.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("Todo title is required");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Todo title must be at most " + MAX_LENGTH + " characters");
        }
    }

    public static TodoTitle of(String value) {
        return new TodoTitle(value);
    }
}
