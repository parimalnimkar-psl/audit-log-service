package com.example.audit.api.dto;

import java.time.Instant;
import java.util.List;

public record AuditExportResponse(
    String format,
    String hashAlgorithm,
    String genesisHash,
    Instant exportedAt,
    int recordCount,
    List<AuditEventResponse> records) {
}