#!/usr/bin/env bash
# Forward Stripe webhook events to your local backend (no browser login needed).
# Usage: ./stripe-listen.sh   (run from platform-back/ or set STRIPE_SECRET_KEY)
set -e
cd "$(dirname "$0")"
if [ -f .env ]; then
  export STRIPE_API_KEY=$(grep '^STRIPE_SECRET_KEY=' .env | sed 's/^STRIPE_SECRET_KEY=//' | tr -d '\r')
fi
if [ -z "$STRIPE_API_KEY" ]; then
  echo "Set STRIPE_SECRET_KEY in .env or STRIPE_API_KEY in the environment."
  exit 1
fi
echo "Forwarding webhooks to http://localhost:9090/api/v1/payments/webhook"
echo "Note: Each run prints a NEW signing secret. Update .env STRIPE_WEBHOOK_SECRET and restart the backend if it changes."
exec stripe listen --forward-to localhost:9090/api/v1/payments/webhook
