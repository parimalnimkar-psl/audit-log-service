package com.example.audit.repository;

import com.example.audit.domain.AuditEvent;

import java.util.*;

import org.springframework.data.jpa.repository.*;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long>, JpaSpecificationExecutor<AuditEvent> {
    Optional<AuditEvent> findTopByOrderByChainSequenceDesc();

    List<AuditEvent> findAllByOrderByChainSequenceAsc();

    @Query(value = "SELECT nextval('audit_chain_sequence')", nativeQuery = true)
    long nextChainSequence();
}
