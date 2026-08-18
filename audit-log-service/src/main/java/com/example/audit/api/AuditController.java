package com.example.audit.api;

import com.example.audit.api.dto.*;
import com.example.audit.service.AuditService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import java.time.Instant;

import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private final AuditService service;

    public AuditController(AuditService s) {
        service = s;
    }

    @PostMapping("/events")
    @PreAuthorize("hasAuthority('SCOPE_AUDIT_WRITER') or hasAuthority('AUDIT_WRITER')")
    public ResponseEntity<AuditEventResponse> create(@Valid @RequestBody CreateAuditEventRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.append(r));
    }

    @GetMapping("/events")
    @PreAuthorize("hasAuthority('SCOPE_AUDIT_READER') or hasAuthority('AUDIT_READER')")
    public Page<AuditEventResponse> query(@RequestParam(required = false) String actorId,
                                        @RequestParam(required = false) String resourceType,
                                        @RequestParam(required = false) String resourceId,
                                        @RequestParam(required = false) String eventType,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                        Authentication authentication,
                                        @PageableDefault(size = 20, sort = "chainSequence", direction = Sort.Direction.ASC) Pageable pageable) {
        String resolvedActorId = actorId;
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> "SCOPE_AUDIT_ADMIN".equals(a.getAuthority()) || "AUDIT_ADMIN".equals(a.getAuthority()));
        if (!isAdmin) {
            resolvedActorId = authentication.getName();
        }
        return service.query(resolvedActorId, resourceType, resourceId, eventType, from, to, pageable);
    }

    @GetMapping("/verify")
    @PreAuthorize("hasAuthority('SCOPE_AUDIT_ADMIN') or hasAuthority('AUDIT_ADMIN')")
    public VerificationResponse verify() {
        return service.verify();
    }
}
