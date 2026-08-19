# Scenario C — Compliance Reporting

## Clarified working statement

The service must provide an authenticated, read-only report of successful and failed access to client-account resources for a customer-approved reporting period. Reports must identify the authenticated actor, event type, resource identifier, UTC event time, payload policy, and chain evidence. The regulator-facing caller must be mapped to an explicit Keycloak role and may access only approved export fields.

## Open decisions and scope boundary

Customer and compliance approval is still required for the exact event taxonomy, account versus transaction scope, reporting period, retention, regulator authorization, export format, and redaction policy. Until those decisions are approved, the current implementation supports the underlying event capture, actor/time/resource filtering, and authorized JSON export but does not claim a regulator-specific report contract.
