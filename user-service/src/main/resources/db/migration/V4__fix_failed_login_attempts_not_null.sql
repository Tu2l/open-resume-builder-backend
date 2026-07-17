-- Backfill any NULL rows that may have been inserted before this constraint,
-- then enforce NOT NULL with a DEFAULT so the column matches the primitive int
-- field on UserAccountStatus and new rows are always initialised to zero.
UPDATE user_account_status SET failed_login_attempts = 0 WHERE failed_login_attempts IS NULL;
ALTER TABLE user_account_status ALTER COLUMN failed_login_attempts SET NOT NULL;
ALTER TABLE user_account_status ALTER COLUMN failed_login_attempts SET DEFAULT 0;
