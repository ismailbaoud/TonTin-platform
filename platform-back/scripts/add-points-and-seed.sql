-- Add points column to users (for trust/ranking system)
-- Run once. Safe to re-run: uses IF NOT EXISTS / DO blocks where needed.

-- Add column if not present (PostgreSQL)
ALTER TABLE users ADD COLUMN IF NOT EXISTS points INT NOT NULL DEFAULT 0;

-- Seed some points so leaderboard is visible (spread 50–500 per user)
-- Only updates users that still have 0 (or were just added) so we don't overwrite real data on re-run
DO $$
DECLARE
  r RECORD;
  seed_pts INT;
BEGIN
  FOR r IN SELECT id FROM users LOOP
    seed_pts := 50 + floor(random() * 451)::int;
    UPDATE users SET points = seed_pts WHERE id = r.id AND (points IS NULL OR points = 0);
  END LOOP;
END $$;
