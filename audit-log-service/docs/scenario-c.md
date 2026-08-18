# Scenario C — Compliance Reporting

The requirement that regulators must audit access to client account data is ambiguous. Before final implementation, clarify event types, successful/failed access, resource scope, reporting period, retention, regulator authorization, required evidence and redaction rules. The current service can record access events using `eventType` and query by resource/actor/time, but a regulator-specific reporting contract should not be invented without clarification.
