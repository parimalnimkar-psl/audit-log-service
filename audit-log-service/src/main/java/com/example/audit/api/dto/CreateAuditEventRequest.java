package com.example.audit.api.dto;
import jakarta.validation.constraints.*;
public record CreateAuditEventRequest(@NotBlank @Size(max=100) String eventType,@NotBlank @Size(max=150) String actorId,@NotBlank @Size(max=100) String resourceType,@NotBlank @Size(max=150) String resourceId,@NotBlank @Size(max=10000) String payload){}
