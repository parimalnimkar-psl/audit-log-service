package com.example.audit.service;

import com.example.audit.api.dto.*;
import com.example.audit.config.AppProperties;
import com.example.audit.domain.AuditEvent;
import com.example.audit.repository.AuditEventRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private static final ReentrantLock APPEND_LOCK = new ReentrantLock();
    private final AuditEventRepository repo;
    private final HashService hashes;
    private final AppProperties props;

    public AuditService(AuditEventRepository r, HashService h, AppProperties p) {
        repo = r;
        hashes = h;
        props = p;
    }

    @Transactional
    public AuditEventResponse append(CreateAuditEventRequest r) {
        APPEND_LOCK.lock();
        try {
            long seq = repo.nextChainSequence();
            String prev = repo.findTopByOrderByChainSequenceDesc().map(AuditEvent::getContentHash).orElse(props.audit().genesisHash());
            Instant ts = Instant.now().truncatedTo(ChronoUnit.MICROS);
            String hash = hashes.hash(hashes.canonical(seq, r.eventType(), r.actorId(), r.resourceType(), r.resourceId(), r.payload(), ts));
            AuditEvent e = repo.save(new AuditEvent(seq, r.eventType(), r.actorId(), r.resourceType(), r.resourceId(), r.payload(), ts, prev, hash));
            log.info("audit_event_created id={} sequence={} actor={} resourceType={} resourceId={}", e.getId(), seq, r.actorId(), r.resourceType(), r.resourceId());
            return AuditEventResponse.from(e);
        } finally {
            APPEND_LOCK.unlock();
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditEventResponse> query(String actor, String rt, String rid, String type, Instant from, Instant to, Pageable page) {
        Specification<AuditEvent> s = Specification.where(null);
        if (actor != null) s = s.and((r, q, c) -> c.equal(r.get("actorId"), actor));
        if (rt != null) s = s.and((r, q, c) -> c.equal(r.get("resourceType"), rt));
        if (rid != null) s = s.and((r, q, c) -> c.equal(r.get("resourceId"), rid));
        if (type != null) s = s.and((r, q, c) -> c.equal(r.get("eventType"), type));
        if (from != null) s = s.and((r, q, c) -> c.greaterThanOrEqualTo(r.get("eventTimestamp"), from));
        if (to != null) s = s.and((r, q, c) -> c.lessThanOrEqualTo(r.get("eventTimestamp"), to));
        return repo.findAll(s, page).map(AuditEventResponse::from);
    }

    @Transactional(readOnly = true)
    public VerificationResponse verify() {
        List<AuditEvent> all = repo.findAllByOrderByChainSequenceAsc();
        String expectedPrev = props.audit().genesisHash();
        long expectedSeq = 1;
        for (AuditEvent e : all) {
            if (e.getChainSequence() != expectedSeq) return broken(e, "SEQUENCE_MISMATCH", "Unexpected chain sequence");
            if (!Objects.equals(e.getPreviousHash(), expectedPrev)) return broken(e, "PREVIOUS_HASH_MISMATCH", "Previous hash does not match chain");
            String actual = hashes.hash(hashes.canonical(
                e.getChainSequence(),
                e.getEventType(),
                e.getActorId(),
                e.getResourceType(),
                e.getResourceId(),
                e.getPayload(),
                e.getEventTimestamp()));
            if (!Objects.equals(actual, e.getContentHash())) return broken(e, "CONTENT_HASH_MISMATCH", "Stored content hash does not match event content");
            expectedPrev = e.getContentHash();
            expectedSeq++;
        }
        return new VerificationResponse(true, all.size(), null, null, null, "Chain is intact");
    }

    @Transactional(readOnly = true)
    public AuditExportResponse export(String actor, String resourceType, String resourceId, String eventType,
                                      Instant from, Instant to) {
        Page<AuditEventResponse> page = query(actor, resourceType, resourceId, eventType, from, to, Pageable.unpaged());
        return new AuditExportResponse("json", "SHA-256", props.audit().genesisHash(), Instant.now(),
            page.getNumberOfElements(), page.getContent());
    }

    private VerificationResponse broken(AuditEvent e, String type, String msg) {
        log.warn("audit_chain_broken id={} sequence={} type={}", e.getId(), e.getChainSequence(), type);
        return new VerificationResponse(false, repo.count(), e.getId(), e.getChainSequence(), type, msg);
    }
}
