-- Fix notifications_type_check so type column accepts all NotificationType API values (lowercase).
-- Run this if you get: "la contrainte de vérification « notifications_type_check »" when creating notifications.

ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;

ALTER TABLE notifications ADD CONSTRAINT notifications_type_check CHECK (
  type IN (
    'payment_due',
    'payment_received',
    'payout_ready',
    'dar_invitation',
    'member_joined',
    'member_left',
    'tour_completed',
    'reminder',
    'system',
    'trust_score',
    'message',
    'warning',
    'information',
    'alert'
  )
);
