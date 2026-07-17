-- Replace unconditional unique constraints on username/email with partial unique
-- indexes scoped to non-deleted rows, so a soft-deleted user's credentials do
-- not block re-registration by anyone else.

alter table users drop constraint uk_username;
alter table users drop constraint uk_email;

create unique index uk_username on users (username) where deleted_at is null;
create unique index uk_email    on users (email)    where deleted_at is null;
