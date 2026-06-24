package org.example.backend.dto;

import java.util.List;

public class ImportDtos {
    public record ImportResult(int success, int failed, List<String> errors) {
    }
}
