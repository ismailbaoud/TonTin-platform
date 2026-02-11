# Payments Feature Module

## 📋 Overview

This is the Payments feature module for TonTin Platform. It handles all payment-related functionality including contribution payments, payment history, and payment status tracking.

**Current Status**: ✅ Hardcoded (Development Mode)  
**API Ready**: ✅ Yes - Easy to switch to backend integration

---

## 🏗️ Architecture

```
features/dashboard/features/payments/
├── pages/
│   └── pay-contribution.component.ts    # Payment form and processing
├── services/
│   └── payment.service.ts               # Payment service (HARDCODED)
├── payments.routes.ts                   # Feature routing
└── README.md                            # This file
```

---

## 🎯 Features

### Current Implementation (Hardcoded)

- ✅ **Pay Contribution**
  - Payment form with validation
  - Multiple payment methods support
  - Amount calculation
  - Payment confirmation
  - Success/error handling

- ✅ **Payment History**
  - List of all payments
  - Filter by status and date
  - Payment details view
  - Receipt download (mock)

- ✅ **Payment Methods**
  - Credit/Debit Card
  - Bank Transfer
  - Mobile Money
  - PayPal

- ✅ **Mock Payment Service**
  - In-memory payment storage
  - Payment processing simulation
  - Status tracking
  - Receipt generation

---

## 🔄 Switching from Hardcoded to API

The payment service is **structured to make API integration simple**. Here's how:

### Current (Hardcoded) Code

```typescript
processPayment(payment: PaymentRequest): Observable<PaymentResponse> {
  // Hardcoded implementation
  return new Observable<PaymentResponse>(observer => {
    setTimeout(() => {
      observer.next(mockResponse);
    }, 2000);
  });
}
```

### Switch to API (3 Steps)

#### Step 1: Inject HttpClient

```typescript
// In payment.service.ts
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../../environments/environment';

constructor(private http: HttpClient) {}
```

#### Step 2: Replace Method Bodies

```typescript
// Process payment
processPayment(payment: PaymentRequest): Observable<PaymentResponse> {
  return this.http.post<PaymentResponse>(
    `${environment.apiUrl}/payments`,
    payment
  );
}

// Get payment history
getPaymentHistory(userId?: number): Observable<Payment[]> {
  return this.http.get<Payment[]>(`${environment.apiUrl}/payments`);
}

// Get payment by ID
getPaymentById(id: number): Observable<Payment> {
  return this.http.get<Payment>(`${environment.apiUrl}/payments/${id}`);
}

// Get payment status
getPaymentStatus(id: number): Observable<PaymentStatus> {
  return this.http.get<PaymentStatus>(
    `${environment.apiUrl}/payments/${id}/status`
  );
}

// Request refund
requestRefund(id: number, reason: string): Observable<RefundResponse> {
  return this.http.post<RefundResponse>(
    `${environment.apiUrl}/payments/${id}/refund`,
    { reason }
  );
}

// Download receipt
downloadReceipt(id: number): Observable<Blob> {
  return this.http.get(`${environment.apiUrl}/payments/${id}/receipt`, {
    responseType: 'blob'
  });
}
```

#### Step 3: Update Environment Config

```typescript
// environment.development.ts
export const environment = {
  apiUrl: 'http://localhost:9090/api',  // Your Spring Boot backend
};
```

**That's it!** The component doesn't need to change at all.

---

## 📝 API Endpoints (When Ready)

### Process Payment
```
POST /api/payments
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "darId": 123,
  "amount": 1000.00,
  "paymentMethod": "CARD",
  "paymentDetails": {
    "cardNumber": "4111111111111111",
    "expiryDate": "12/25",
    "cvv": "123",
    "cardHolderName": "John Doe"
  },
  "currency": "USD"
}

Response (201):
{
  "id": 1,
  "darId": 123,
  "amount": 1000.00,
  "status": "PENDING",
  "transactionId": "TXN-123456789",
  "createdAt": "2024-01-15T10:30:00Z",
  "estimatedCompletionTime": "2024-01-15T10:35:00Z"
}
```

### Get Payment History
```
GET /api/payments
Authorization: Bearer {token}

Query Parameters:
- userId: number (optional)
- darId: number (optional)
- status: string (optional) - PENDING, COMPLETED, FAILED, REFUNDED
- startDate: string (optional) - ISO date
- endDate: string (optional) - ISO date
- page: number (default: 0)
- size: number (default: 20)

Response (200):
{
  "data": [
    {
      "id": 1,
      "darId": 123,
      "darName": "Family Savings",
      "amount": 1000.00,
      "status": "COMPLETED",
      "paymentMethod": "CARD",
      "transactionId": "TXN-123456789",
      "createdAt": "2024-01-15T10:30:00Z",
      "completedAt": "2024-01-15T10:32:00Z",
      "receiptUrl": "/api/payments/1/receipt"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "total": 50,
    "totalPages": 3
  }
}
```

### Get Payment by ID
```
GET /api/payments/{id}
Authorization: Bearer {token}

Response (200):
{
  "id": 1,
  "darId": 123,
  "darName": "Family Savings",
  "userId": 101,
  "userName": "John Doe",
  "amount": 1000.00,
  "status": "COMPLETED",
  "paymentMethod": "CARD",
  "transactionId": "TXN-123456789",
  "createdAt": "2024-01-15T10:30:00Z",
  "completedAt": "2024-01-15T10:32:00Z",
  "failureReason": null,
  "metadata": {
    "ipAddress": "192.168.1.1",
    "userAgent": "Mozilla/5.0..."
  }
}
```

### Get Payment Status
```
GET /api/payments/{id}/status
Authorization: Bearer {token}

Response (200):
{
  "paymentId": 1,
  "status": "COMPLETED",
  "statusCode": "SUCCESS",
  "message": "Payment completed successfully",
  "lastUpdated": "2024-01-15T10:32:00Z"
}
```

### Request Refund
```
POST /api/payments/{id}/refund
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "reason": "Duplicate payment"
}

Response (200):
{
  "refundId": 1,
  "paymentId": 1,
  "amount": 1000.00,
  "status": "PENDING",
  "reason": "Duplicate payment",
  "createdAt": "2024-01-15T11:00:00Z",
  "estimatedCompletionTime": "2024-01-20T11:00:00Z"
}
```

### Download Receipt
```
GET /api/payments/{id}/receipt
Authorization: Bearer {token}

Response (200):
Content-Type: application/pdf
Content-Disposition: attachment; filename="receipt-TXN-123456789.pdf"

[PDF Binary Data]
```

---

## 📦 Models/Interfaces

All TypeScript interfaces are defined in `payment.service.ts`:

```typescript
interface Payment {
  id: number;
  darId: number;
  darName: string;
  userId: number;
  userName: string;
  amount: number;
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';
  paymentMethod: 'CARD' | 'BANK_TRANSFER' | 'MOBILE_MONEY' | 'PAYPAL';
  transactionId: string;
  createdAt: string;
  completedAt?: string;
  failureReason?: string;
  receiptUrl?: string;
}

interface PaymentRequest {
  darId: number;
  amount: number;
  paymentMethod: string;
  paymentDetails: PaymentDetails;
  currency: string;
}

interface PaymentDetails {
  cardNumber?: string;
  expiryDate?: string;
  cvv?: string;
  cardHolderName?: string;
  bankAccountNumber?: string;
  routingNumber?: string;
  mobileMoneyNumber?: string;
  paypalEmail?: string;
}

interface PaymentResponse {
  id: number;
  darId: number;
  amount: number;
  status: string;
  transactionId: string;
  createdAt: string;
  estimatedCompletionTime?: string;
}

interface PaymentStatus {
  paymentId: number;
  status: string;
  statusCode: string;
  message: string;
  lastUpdated: string;
}

interface RefundResponse {
  refundId: number;
  paymentId: number;
  amount: number;
  status: string;
  reason: string;
  createdAt: string;
  estimatedCompletionTime: string;
}
```

**These should match your Spring Boot DTOs** - Update them if your backend uses different field names.

---

## 🎨 Components

### Pay Contribution Component
- Payment form with validation
- Multiple payment method options
- Amount display and calculation
- Payment method selection
- Secure payment processing
- Success/failure feedback
- Receipt generation

---

## 🔐 Security & Permissions

### Current (Hardcoded)
- Mock payment processing
- No real transaction handling
- Simulated payment methods

### When Connecting to API
- ✅ PCI DSS compliance
- ✅ Encrypted payment data
- ✅ Secure HTTPS connection
- ✅ JWT token authentication
- ✅ Payment method tokenization
- ✅ 3D Secure support
- ✅ Fraud detection
- ✅ Transaction logging

---

## 💳 Payment Methods Integration

### Stripe Integration Example

```typescript
// Install: npm install @stripe/stripe-js

import { loadStripe } from '@stripe/stripe-js';

async processStripePayment(amount: number, darId: number) {
  const stripe = await loadStripe(environment.stripePublicKey);
  
  // Create payment intent on backend
  const paymentIntent = await this.http.post(
    `${environment.apiUrl}/payments/stripe/create-intent`,
    { amount, darId }
  ).toPromise();
  
  // Confirm payment
  const result = await stripe.confirmCardPayment(
    paymentIntent.clientSecret,
    {
      payment_method: {
        card: cardElement,
        billing_details: { name: 'John Doe' }
      }
    }
  );
  
  if (result.error) {
    // Handle error
  } else {
    // Payment successful
  }
}
```

### PayPal Integration Example

```typescript
// Install: npm install @paypal/checkout-server-sdk

loadPayPalScript() {
  const script = document.createElement('script');
  script.src = `https://www.paypal.com/sdk/js?client-id=${environment.paypalClientId}`;
  document.body.appendChild(script);
}

createPayPalOrder(amount: number, darId: number) {
  return this.http.post(
    `${environment.apiUrl}/payments/paypal/create-order`,
    { amount, darId }
  );
}
```

---

## 🚀 Next Steps

### To Complete Payments Feature

1. **Add Payment Gateway Integration**
   - Stripe integration
   - PayPal integration
   - Mobile money providers
   - Bank transfer support

2. **Add Payment Validation**
   - Card number validation
   - Expiry date validation
   - CVV validation
   - Amount validation

3. **Add Payment History**
   - Detailed payment list
   - Advanced filtering
   - Export to CSV/PDF
   - Receipt management

4. **Connect to Backend API**
   - Follow "Switching to API" guide above
   - Test with real Spring Boot backend
   - Handle API errors properly
   - Implement secure payment flow

5. **Add Security Features**
   - Payment tokenization
   - 3D Secure support
   - Fraud detection
   - Transaction monitoring

---

## 📚 Related Files

- **Routes**: `src/app/app.routes.ts`
- **Environment Config**: `src/environments/environment.development.ts`
- **Auth Service**: `src/app/features/auth/services/auth.service.ts`
- **Dâr Service**: `src/app/features/dashboard/features/dars/services/dar.service.ts`
- **Notification Service**: `src/app/features/dashboard/features/notifications/services/notification.service.ts`

---

## 💡 Tips

### Development
```bash
# Access Payment page
http://localhost:4200/dashboard/client/pay-contribution

# Access Payment for specific Dâr
http://localhost:4200/dashboard/client/pay-contribution/123
```

### Debugging
```typescript
// Enable debug logs in payment.service.ts
console.log('💳 Processing payment:', payment);
console.log('✅ Payment completed:', response);
console.log('❌ Payment failed:', error);
```

### Testing Payment Flow
```typescript
// Test card numbers (Stripe)
const testCards = {
  success: '4242424242424242',
  declined: '4000000000000002',
  insufficient: '4000000000009995',
  expired: '4000000000000069'
};
```

---

## 🤝 Contributing

When adding new payment features:

1. Keep the **hardcoded/API switch pattern**
2. Add **loading states** for better UX
3. Include **comprehensive error handling**
4. Add **payment validation**
5. Implement **secure payment processing**
6. Update this **README**

---

## ✅ Checklist for API Integration

- [ ] Backend API is running (`http://localhost:9090`)
- [ ] API endpoints match the ones documented above
- [ ] Environment file has correct `apiUrl`
- [ ] Payment gateway credentials configured
- [ ] HttpClient is injected in payment.service.ts
- [ ] All methods use `http.post/get()`
- [ ] Error responses match expected format
- [ ] CORS is configured on backend
- [ ] SSL/HTTPS is enabled
- [ ] PCI compliance requirements met
- [ ] Test all payment methods
- [ ] Handle payment failures properly
- [ ] Add loading states
- [ ] Add error messages
- [ ] Test refund flow
- [ ] Test receipt download

---

**Status**: Ready for development ✅  
**Last Updated**: February 2025  
**Version**: 1.0.0