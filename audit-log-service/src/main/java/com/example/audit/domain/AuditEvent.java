package com.example.audit.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "audit_events", indexes = {@Index(name = "idx_audit_actor_time", columnList = "actor_id,event_timestamp"), @Index(name = "idx_audit_resource_time", columnList = "resource_type,resource_id,event_timestamp")})
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "chain_sequence", nullable = false, unique = true)
    private long chainSequence;
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    @Column(name = "actor_id", nullable = false, length = 150)
    private String actorId;
    @Column(name = "resource_type", nullable = false, length = 100)
    private String resourceType;
    @Column(name = "resource_id", nullable = false, length = 150)
    private String resourceId;
    @Column(nullable = false, length = 10000)
    private String payload;
    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;
    @Column(name = "previous_hash", nullable = false, length = 64)
    private String previousHash;
    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;
    @Column(name = "hash_algorithm", nullable = false, length = 30)
    private String hashAlgorithm = "SHA-256";
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditStatus status = AuditStatus.ACTIVE;
    @Column(name = "redaction_metadata", length = 5000)
    private String redactionMetadata;

    protected AuditEvent() {
    }

    public AuditEvent(long seq, String type, String actor, String rt, String rid, String payload, Instant ts, String prev, String hash) {
        this.chainSequence = seq;
        this.eventType = type;
        this.actorId = actor;
        this.resourceType = rt;
        this.resourceId = rid;
        this.payload = payload;
        this.eventTimestamp = ts;
        this.previousHash = prev;
        this.contentHash = hash;
    }

    public Long getId() {
        return id;
    }

    public long getChainSequence() {
        return chainSequence;
    }

    public String getEventType() {
        return eventType;
    }

    public String getActorId() {
        return actorId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getEventTimestamp() {
        return eventTimestamp;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getContentHash() {
        return contentHash;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public AuditStatus getStatus() {
        return status;
    }

    public String getRedactionMetadata() {
        return redactionMetadata;
    }
}
