-- One-time migration: change picture column from oid (LOB) to bytea.
-- Run this against your PostgreSQL database if you get:
--   "la colonne « picture » est de type oid mais l'expression est de type bytea"
--
-- Existing picture data will be set to NULL (oid cannot be auto-converted to bytea).
-- New picture uploads will then work with bytea.

-- darts table
ALTER TABLE darts
  ALTER COLUMN picture TYPE bytea USING NULL;

-- users table
ALTER TABLE users
  ALTER COLUMN picture TYPE bytea USING NULL;
