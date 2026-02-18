-- ========================================
-- DATABASE MIGRATION: Make start_date Nullable
-- ========================================
-- Date: February 2024
-- Issue: Creating darts failed with "start_date cannot be null" error
-- Solution: Remove NOT NULL constraint from start_date column
--
-- Reason: start_date should be NULL when dart is created,
--         and only set when organizer clicks "Start Dart" button
-- ========================================

-- Migration command (already applied):
ALTER TABLE darts ALTER COLUMN start_date DROP NOT NULL;

-- Verification:
-- SELECT column_name, is_nullable, data_type
-- FROM information_schema.columns
-- WHERE table_name = 'darts' AND column_name = 'start_date';
--
-- Expected result:
-- column_name  | is_nullable | data_type
-- start_date   | YES         | timestamp without time zone

-- ========================================
-- BEFORE:
-- start_date | timestamp(6) without time zone | not null
--
-- AFTER:
-- start_date | timestamp(6) without time zone | (nullable)
-- ========================================

-- Test query to verify darts can be created with NULL start_date:
-- INSERT INTO darts (id, name, monthly_contribution, order_method,
--                    payment_frequency, status, created_at, updated_at, version)
-- VALUES (gen_random_uuid(), 'Test Dart', 10.00, 'FIXED_ORDER',
--         'MONTH', 'PENDING', NOW(), NOW(), 0);
--
-- This should succeed now (previously would fail)

-- ========================================
-- Related Changes:
-- ========================================
-- 1. Backend: platform-back/src/main/java/com/tontin/platform/domain/Dart.java
--    - Removed @NotNull annotation from startDate field
--    - Changed @Column(nullable = false) to @Column(nullable = true)
--
-- 2. Backend: platform-back/src/main/java/com/tontin/platform/service/impl/DartServiceImpl.java
--    - Already sets dart.setStartDate(null) on creation
--    - startDate is set only when organizer calls startDart() method
--
-- ========================================
-- Lifecycle:
-- ========================================
-- 1. CREATE: Organizer creates dart
--    → start_date = NULL
--    → status = PENDING
--
-- 2. START: Organizer clicks "Start Dart" button
--    → start_date = CURRENT_TIMESTAMP
--    → status = ACTIVE
--
-- 3. FINISH: All cycles completed
--    → status = FINISHED
-- ========================================

-- Status: ✅ APPLIED SUCCESSFULLY
-- Database: tontin_test
-- Date Applied: February 16, 2026 15:40:00 (approx)
