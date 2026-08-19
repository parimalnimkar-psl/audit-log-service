package com.example.audit.api.dto;

import jakarta.validation.constraints.*;

public record CreateAuditEventRequest(@NotBlank @Size(max = 100) @Pattern(regexp = "[^\\r\\n]*") String eventType,
                                      @NotBlank @Size(max = 150) @Pattern(regexp = "[^\\r\\n]*") String actorId,
                                      @NotBlank @Size(max = 100) @Pattern(regexp = "[^\\r\\n]*") String resourceType,
                                      @NotBlank @Size(max = 150) @Pattern(regexp = "[^\\r\\n]*") String resourceId,
                                      @NotBlank @Size(max = 10000) String payload) {
}
