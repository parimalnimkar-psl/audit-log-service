package com.example.audit.api.dto;

public record VerificationResponse(boolean intact, long checkedRecordCount, Long firstBrokenRecordId,
                                   Long firstBrokenSequence, String violationType, String message) {
}
