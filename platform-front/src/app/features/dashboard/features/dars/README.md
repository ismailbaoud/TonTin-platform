# Dârs Feature Module

## 📋 Overview

This is the Dârs management feature module for TonTin Platform. It handles all Dâr-related functionality including viewing, creating, and managing Dârs (Digital Autonomous Rotating savings groups).

**Current Status**: ✅ Hardcoded (Development Mode)  
**API Ready**: ✅ Yes - Easy to switch to backend integration

---

## 🏗️ Architecture

```
features/dashboard/features/dars/
├── pages/
│   ├── my-dars.component.ts       # List of user's Dârs
│   ├── create-dar.component.ts    # Create new Dâr form
│   └── dar-details.component.ts   # Detailed Dâr view
├── services/
│   └── dar.service.ts             # Dâr data service (HARDCODED)
├── dars.routes.ts                 # Feature routing
└── README.md                      # This file
```

---

## 🎯 Features

### Current Implementation (Hardcoded)

- ✅ **My Dârs List**
  - View all user's Dârs
  - Filter by status (Active, Pending, Completed)
  - Quick stats overview
  - Navigation to Dâr details

- ✅ **Create Dâr**
  - Form validation
  - Dâr configuration
  - Member management
  - Schedule setup
  - Success/error handling

- ✅ **Dâr Details**
  - Comprehensive Dâr information
  - Member list and status
  - Payment schedule
  - Transaction history
  - Real-time updates

- ✅ **Mock Dâr Service**
  - In-memory Dâr storage
  - CRUD operations
  - Status management
  - Member management
  - Mock API responses

---

## 🔄 Switching from Hardcoded to API

The Dâr service is **structured to make API integration simple**. Here's how:

### Current (Hardcoded) Code

```typescript
getDars(): Observable<Dar[]> {
  // Hardcoded implementation
  return new Observable<Dar[]>(observer => {
    setTimeout(() => {
      observer.next(this.mockDars);
    }, 500);
  });
}
```

### Switch to API (3 Steps)

#### Step 1: Inject HttpClient

```typescript
// In dar.service.ts
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';

constructor(private http: HttpClient) {}
```

#### Step 2: Replace Method Bodies

```typescript
// Get all Dârs
getDars(): Observable<Dar[]> {
  return this.http.get<Dar[]>(`${environment.apiUrl}/dars`);
}

// Get Dâr by ID
getDarById(id: number): Observable<Dar> {
  return this.http.get<Dar>(`${environment.apiUrl}/dars/${id}`);
}

// Create Dâr
createDar(dar: CreateDarRequest): Observable<Dar> {
  return this.http.post<Dar>(`${environment.apiUrl}/dars`, dar);
}

// Update Dâr
updateDar(id: number, dar: UpdateDarRequest): Observable<Dar> {
  return this.http.put<Dar>(`${environment.apiUrl}/dars/${id}`, dar);
}

// Delete Dâr
deleteDar(id: number): Observable<void> {
  return this.http.delete<void>(`${environment.apiUrl}/dars/${id}`);
}
```

#### Step 3: Update Environment Config

```typescript
// environment.development.ts
export const environment = {
  apiUrl: 'http://localhost:9090/api',  // Your Spring Boot backend
};
```

**That's it!** The components don't need to change at all.

---

## 📝 API Endpoints (When Ready)

### Get All Dârs
```
GET /api/dars
Authorization: Bearer {token}

Response (200):
{
  "data": [
    {
      "id": 1,
      "name": "Family Savings",
      "description": "Monthly family savings group",
      "totalAmount": 10000.00,
      "contributionAmount": 1000.00,
      "frequency": "MONTHLY",
      "status": "ACTIVE",
      "startDate": "2024-01-01",
      "endDate": "2024-10-01",
      "memberCount": 10,
      "currentRound": 3,
      "createdAt": "2023-12-01T00:00:00Z"
    }
  ]
}
```

### Get Dâr by ID
```
GET /api/dars/{id}
Authorization: Bearer {token}

Response (200):
{
  "id": 1,
  "name": "Family Savings",
  "description": "Monthly family savings group",
  "totalAmount": 10000.00,
  "contributionAmount": 1000.00,
  "frequency": "MONTHLY",
  "status": "ACTIVE",
  "startDate": "2024-01-01",
  "endDate": "2024-10-01",
  "members": [
    {
      "id": 1,
      "userId": 101,
      "username": "john_doe",
      "email": "john@example.com",
      "joinedAt": "2023-12-01T00:00:00Z",
      "status": "ACTIVE",
      "paymentStatus": "PAID"
    }
  ],
  "schedule": [
    {
      "round": 1,
      "dueDate": "2024-01-15",
      "recipientId": 101,
      "status": "COMPLETED"
    }
  ]
}
```

### Create Dâr
```
POST /api/dars
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "name": "string",
  "description": "string",
  "contributionAmount": 1000.00,
  "frequency": "MONTHLY",
  "startDate": "2024-01-01",
  "maxMembers": 10
}

Response (201):
{
  "id": 1,
  "name": "Family Savings",
  "status": "PENDING",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

### Update Dâr
```
PUT /api/dars/{id}
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "name": "string",
  "description": "string",
  "status": "ACTIVE"
}

Response (200):
{
  "id": 1,
  "name": "Updated Name",
  "status": "ACTIVE",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### Delete Dâr
```
DELETE /api/dars/{id}
Authorization: Bearer {token}

Response (204): No Content
```

---

## 📦 Models/Interfaces

All TypeScript interfaces are defined in `dar.service.ts`:

```typescript
interface Dar {
  id: number;
  name: string;
  description: string;
  totalAmount: number;
  contributionAmount: number;
  frequency: 'WEEKLY' | 'MONTHLY' | 'QUARTERLY';
  status: 'PENDING' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
  startDate: string;
  endDate: string;
  memberCount: number;
  currentRound: number;
  createdAt: string;
}

interface DarMember {
  id: number;
  userId: number;
  username: string;
  email: string;
  joinedAt: string;
  status: 'ACTIVE' | 'INACTIVE' | 'PENDING';
  paymentStatus: 'PAID' | 'PENDING' | 'OVERDUE';
}

interface CreateDarRequest {
  name: string;
  description: string;
  contributionAmount: number;
  frequency: string;
  startDate: string;
  maxMembers: number;
}

interface UpdateDarRequest {
  name?: string;
  description?: string;
  status?: string;
}
```

**These should match your Spring Boot DTOs** - Update them if your backend uses different field names.

---

## 🎨 Components

### My Dârs Component
- Lists all user's Dârs
- Filters by status
- Shows quick stats
- Navigation to details

### Create Dâr Component
- Multi-step form
- Validation
- Member invitation
- Schedule configuration

### Dâr Details Component
- Comprehensive information
- Member management
- Payment tracking
- Transaction history

---

## 🔐 Security & Permissions

### Current (Hardcoded)
- Basic mock authorization
- No real permission checks

### When Connecting to API
- ✅ JWT token authentication
- ✅ Role-based access control
- ✅ Owner/Admin permissions
- ✅ Member-only data access
- ✅ Audit logging

---

## 🚀 Next Steps

### To Complete Dârs Feature

1. **Add Real-time Updates**
   - WebSocket integration
   - Live payment notifications
   - Member status updates

2. **Add Advanced Filtering**
   - Date range filters
   - Amount range filters
   - Member filters
   - Search functionality

3. **Add Bulk Operations**
   - Bulk member invitations
   - Batch payment processing
   - Export to CSV/PDF

4. **Connect to Backend API**
   - Follow "Switching to API" guide above
   - Test with real Spring Boot backend
   - Handle API errors properly

5. **Add Analytics**
   - Payment trends
   - Member activity
   - Success rate metrics

---

## 📚 Related Files

- **Routes**: `src/app/app.routes.ts`
- **Environment Config**: `src/environments/environment.development.ts`
- **Auth Service**: `src/app/features/auth/services/auth.service.ts`
- **Payment Service**: `src/app/features/dashboard/features/payments/services/payment.service.ts`

---

## 💡 Tips

### Development
```bash
# Access My Dârs page
http://localhost:4200/dashboard/client/my-dars

# Access Create Dâr page
http://localhost:4200/dashboard/client/create-dar

# Access Dâr Details page
http://localhost:4200/dashboard/client/dar/1
```

### Debugging
```typescript
// Enable debug logs in dar.service.ts
console.log('📋 Fetching Dârs:', dars);
console.log('✅ Dâr created:', response);
```

---

## 🤝 Contributing

When adding new Dâr features:

1. Keep the **hardcoded/API switch pattern**
2. Add **loading states** for better UX
3. Include **error handling**
4. Add **form validation**
5. Update this **README**

---

## ✅ Checklist for API Integration

- [ ] Backend API is running (`http://localhost:9090`)
- [ ] API endpoints match the ones documented above
- [ ] Environment file has correct `apiUrl`
- [ ] HttpClient is injected in dar.service.ts
- [ ] All methods use `http.get/post/put/delete()`
- [ ] Error responses match expected format
- [ ] CORS is configured on backend
- [ ] Test all CRUD operations
- [ ] Handle API errors properly
- [ ] Add loading states
- [ ] Add error messages

---

**Status**: Ready for development ✅  
**Last Updated**: February 2025  
**Version**: 1.0.0