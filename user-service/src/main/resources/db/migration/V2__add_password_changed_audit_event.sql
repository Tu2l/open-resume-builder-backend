-- Allow the new PASSWORD_CHANGED audit event type (emitted on self-service password change).
-- The V1 baseline declared an inline CHECK on audit_events.event_type, which Postgres
-- auto-named audit_events_event_type_check; drop and recreate it with the extra value.
alter table audit_events
    drop constraint audit_events_event_type_check;

alter table audit_events
    add constraint audit_events_event_type_check
        check (event_type in (
            'USER_REGISTERED', 'LOGIN_SUCCESS', 'LOGIN_FAILED', 'ACCOUNT_LOCKED',
            'ACCOUNT_UNLOCKED', 'ACCOUNT_ENABLED', 'ACCOUNT_DISABLED', 'VERIFICATION_RESENT',
            'LOGOUT', 'TOKEN_REFRESHED', 'TOKEN_REUSE_DETECTED', 'PASSWORD_RESET_REQUESTED',
            'PASSWORD_RESET_COMPLETED', 'PASSWORD_CHANGED', 'EMAIL_VERIFIED', 'ROLE_ASSIGNED',
            'PROFILE_UPDATED'
        ));
