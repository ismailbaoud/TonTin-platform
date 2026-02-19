# Pay Contribution Feature Documentation

## Overview

The Pay Contribution feature allows users to make payments for their Dâr (tontine) commitments. This feature provides a seamless payment experience with multiple payment methods, real-time contribution tracking, and activity history.

## Feature Components

### 1. Pay Contribution Page Component

**Location:** `platform-front/src/app/features/dashboard/pages/pay-contribution/`

**Files:**
- `pay-contribution.component.ts` - Component logic and data management
- `pay-contribution.component.html` - Template with payment form and UI
- `pay-contribution.component.scss` - Styles and animations

### 2. Key Features

#### A. Dâr Selection
- Dropdown to select which Dâr circle to contribute to
- Auto-loads contribution details for selected Dâr
- Displays current cycle information

#### B. Current Cycle Information Card
- Shows cycle number (e.g., "Cycle #4 of 12")
- Displays current beneficiary with avatar
- Shows pot size for current cycle
- Displays due date and payment status
- Progress bar showing contribution completion
- Member contribution count

#### C. Early Contribution Bonus
- Alerts users when contributing early (3+ days before due date)
- Shows trust score boost (+5 points)
- Encourages timely payments

#### D. Payment Form
- **Contribution Amount:** Fixed amount input with currency display
- **Payment Method Selection:** Three options with visual cards
  - TonTin Wallet (shows balance)
  - Mobile Money (instant processing)
  - Bank Transfer (1-2 days processing)
- **Payment Summary:**
  - Subtotal
  - Platform fee (0.5%)
  - Total payment amount
- **Pay Now Button:** Primary action with smooth animations
- **Security Badge:** SSL encryption indicator

#### E. Recent Activity Table
- Shows last 3 contributions for the selected Dâr
- Columns: Date, Cycle, Beneficiary (with avatar), Status, Amount
- "View All History" link for full transaction history

## Routing

### Routes Added

```typescript
// General pay contribution (no specific Dâr)
{
  path: "pay-contribution",
  component: PayContributionComponent,
  data: { title: "Pay Contribution - TonTin" }
}

// Pay contribution for specific Dâr
{
  path: "pay-contribution/:id",
  component: PayContributionComponent,
  data: { title: "Pay Contribution - TonTin" }
}
```

### Navigation Paths

Users can reach the Pay Contribution page from:
1. **Dâr Details Page:** "Pay Contribution" button in header actions
2. **My Dârs Page:** "Pay Now" button on Dâr cards (when payment is due)
3. **Direct URL:** `/dashboard/client/pay-contribution/:id`

## Data Structures

### Dar Interface
```typescript
interface Dar {
  id: number;
  name: string;
  currentCycle: number;
  totalCycles: number;
  beneficiary: {
    name: string;
    avatar: string;
  };
  potSize: number;
  dueDate: string;
  status: "pending" | "completed" | "late";
  contributionProgress: number;
  membersContributed: number;
  totalMembers: number;
  contributionAmount: number;
}
```

### PaymentMethod Interface
```typescript
interface PaymentMethod {
  id: string;
  name: string;
  icon: string;
  balance?: string;
  processingTime?: string;
}
```

### ActivityRecord Interface
```typescript
interface ActivityRecord {
  date: string;
  cycle: string;
  beneficiary: {
    name: string;
    avatar: string;
  };
  status: string;
  amount: number;
}
```

## Key Methods

### Component Methods

#### `ngOnInit()`
- Retrieves `darId` from route parameters
- Loads Dâr details from service (TODO: backend integration)
- Sets selected Dâr if ID provided

#### `onDarChange(event: Event)`
- Handles Dâr selection change
- Updates contribution amount based on selected Dâr

#### `onPaymentMethodChange(methodId: string)`
- Updates selected payment method
- Handles payment method validation

#### `onPayNow()`
- Processes payment submission
- Validates payment details
- Calls payment API (TODO: backend integration)
- Shows success/error notifications
- Navigates to confirmation or Dâr details page

#### `onViewAllHistory()`
- Navigates to full transaction history page
- Or opens history modal

### Computed Properties

#### `selectedDar`
- Returns currently selected Dâr object
- Used throughout template for displaying Dâr info

#### `platformFee`
- Calculates 0.5% platform fee
- Returns: `contributionAmount * 0.005`

#### `totalPayment`
- Calculates total payment including fee
- Returns: `contributionAmount + platformFee`

#### `daysUntilDue`
- Calculates days remaining until due date
- Used for early contribution bonus logic

#### `isEarlyContribution`
- Returns `true` if payment is 3+ days before due date
- Triggers early contribution bonus display

## UI/UX Features

### Responsive Design
- Mobile-first approach
- Grid layout adapts: 1 column (mobile) → 12-column grid (desktop)
- Left column: Dâr info (5 columns on desktop)
- Right column: Payment form (7 columns on desktop)

### Dark Mode Support
- Full dark mode compatibility
- All colors have dark variants
- Uses Tailwind's dark: prefix

### Animations
- Button hover effects
- Payment method card selection animations
- Progress bar transitions
- Ripple effect on button click
- Slide-in animations for content

### Visual Feedback
- Selected payment method highlighted with:
  - Border color change (primary)
  - Background tint
  - Check icon appears
  - Ring effect
- Form validation states
- Loading states (TODO)
- Success/error messages (TODO)

## Integration Points

### Backend API Endpoints (TODO)

```typescript
// Get Dâr details
GET /api/dars/:id

// Get user's Dârs
GET /api/users/current/dars

// Submit payment
POST /api/payments/contribution
Body: {
  darId: number,
  amount: number,
  paymentMethod: string,
  cycleNumber: number
}

// Get payment history
GET /api/dars/:id/payments

// Validate payment
POST /api/payments/validate
Body: {
  darId: number,
  amount: number,
  paymentMethod: string
}
```

### Services to Create

1. **PaymentService**
   - `submitContribution()`
   - `validatePayment()`
   - `getPaymentMethods()`
   - `getPaymentHistory()`

2. **DarService** (extend existing)
   - `getDarById()`
   - `getDarContributionDetails()`
   - `getUserActiveDars()`

3. **WalletService**
   - `getBalance()`
   - `deductFunds()`
   - `addTransaction()`

## Security Considerations

1. **SSL Encryption:** All transactions encrypted
2. **Payment Validation:** Server-side validation required
3. **Authentication:** Protected by authGuard
4. **Authorization:** User must be member of the Dâr
5. **Idempotency:** Prevent duplicate payments
6. **Amount Verification:** Validate contribution amount matches Dâr rules

## Testing Checklist

### Unit Tests
- [ ] Component initialization with/without darId
- [ ] Dâr selection change
- [ ] Payment method selection
- [ ] Amount calculation (subtotal, fee, total)
- [ ] Early contribution bonus logic
- [ ] Payment submission

### Integration Tests
- [ ] Route navigation from Dâr details
- [ ] Route navigation from My Dârs
- [ ] API calls for Dâr data
- [ ] Payment processing flow
- [ ] Error handling

### E2E Tests
- [ ] Complete payment flow
- [ ] Multiple payment methods
- [ ] Form validation
- [ ] Navigation between pages
- [ ] Responsive behavior

## User Flows

### Flow 1: Pay from Dâr Details
1. User views Dâr details page
2. Clicks "Pay Contribution" button
3. Redirected to pay-contribution page with darId
4. Form pre-populated with Dâr info
5. User selects payment method
6. Clicks "Pay Now"
7. Payment processed
8. Redirected back to Dâr details with success message

### Flow 2: Pay from My Dârs
1. User views "My Dârs" page
2. Sees Dâr card with "Payment Due" indicator
3. Clicks "Pay Now" button
4. Redirected to pay-contribution page
5. (Same as Flow 1, steps 4-8)

### Flow 3: Browse and Pay
1. User navigates to pay-contribution page
2. Selects Dâr from dropdown
3. Reviews contribution details
4. (Same as Flow 1, steps 5-8)

## Mock Data

Currently uses mock data for:
- 3 sample Dârs (Family, Office, Travel)
- 3 payment methods (Wallet, Mobile, Bank)
- 3 recent activity records
- User profile information

**Note:** All mock data should be replaced with API calls in production.

## Future Enhancements

1. **Payment Confirmation Modal**
   - Review before final submission
   - Show detailed breakdown
   - Terms and conditions acceptance

2. **Recurring Payments**
   - Auto-pay setup
   - Schedule future payments
   - Payment reminders

3. **Payment History**
   - Full transaction history page
   - Export to CSV/PDF
   - Filter by date, Dâr, status

4. **Multiple Contributions**
   - Pay for multiple Dârs at once
   - Bulk payment discount
   - Cart-like interface

5. **Split Payments**
   - Pay with multiple methods
   - Wallet + Card combination
   - Installment plans

6. **Payment Notifications**
   - SMS/Email confirmation
   - Push notifications
   - Receipt generation

7. **Trust Score Dashboard**
   - View trust score details
   - See how early payments affect score
   - Score history chart

8. **Payment Analytics**
   - Personal contribution statistics
   - Comparison with other members
   - Spending trends

## Styling

### Color Scheme
- **Primary:** `#13ec5b` (Green)
- **Primary Dark:** `#0ea33f`
- **Background Light:** `#f6f8f6`
- **Background Dark:** `#102216`
- **Success:** Green variants
- **Warning:** Amber variants
- **Error:** Red variants

### Typography
- **Font Family:** Plus Jakarta Sans, Noto Sans
- **Heading:** Bold, tight tracking
- **Body:** Normal weight, regular spacing
- **Monospace:** For amounts (font-mono)

### Spacing
- Consistent padding: 4, 6, 8 units
- Gap between elements: 3, 4, 6 units
- Border radius: lg (0.5rem), xl (0.75rem)

## Accessibility

- ARIA labels on interactive elements
- Keyboard navigation support
- Focus states on all interactive elements
- Screen reader friendly
- High contrast in dark mode
- Sufficient color contrast ratios

## Browser Support

- Chrome/Edge (latest 2 versions)
- Firefox (latest 2 versions)
- Safari (latest 2 versions)
- Mobile browsers (iOS Safari, Chrome Mobile)

## Dependencies

- **Angular:** ^17.0.0
- **Tailwind CSS:** ^3.0.0
- **Material Symbols:** For icons
- **RxJS:** For reactive programming
- **Angular Router:** For navigation
- **Angular Forms:** For form handling

## Deployment Notes

1. Ensure all environment variables are set
2. Configure payment gateway credentials
3. Set up SSL certificates
4. Test payment flows in staging
5. Enable payment logging/monitoring
6. Set up error tracking (Sentry, etc.)
7. Configure rate limiting for payment endpoints

## Support

For issues or questions:
- Check console logs for errors
- Review network tab for API failures
- Verify user authentication state
- Ensure Dâr membership is valid
- Contact backend team for payment gateway issues

---

**Last Updated:** 2024
**Version:** 1.0.0
**Status:** ✅ Complete - Ready for Backend Integration