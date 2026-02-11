import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface User {
  id: number;
  userName: string;
  email: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  avatar?: string;
  bio?: string;
  location?: string;
  dateOfBirth?: string;
  gender?: string;
  language: string;
  timezone: string;
  role: 'ROLE_CLIENT' | 'ROLE_ADMIN';
  trustScore: number;
  isVerified: boolean;
  emailVerified: boolean;
  phoneVerified: boolean;
  kycStatus: 'not_started' | 'pending' | 'verified' | 'rejected';
  accountStatus: 'active' | 'suspended' | 'deactivated';
  createdDate: string;
  lastLoginDate?: string;
  settings?: UserSettings;
}

export interface UserSettings {
  theme: 'light' | 'dark' | 'system';
  emailNotifications: boolean;
  pushNotifications: boolean;
  smsNotifications: boolean;
  twoFactorEnabled: boolean;
  privacyLevel: 'public' | 'friends' | 'private';
  showEmail: boolean;
  showPhone: boolean;
  showLocation: boolean;
  currency: string;
  dateFormat: string;
  timeFormat: '12h' | '24h';
}

export interface UserProfile {
  user: User;
  statistics: UserStatistics;
  badges: Badge[];
  recentActivity: Activity[];
}

export interface UserStatistics {
  totalDars: number;
  activeDars: number;
  completedDars: number;
  organizedDars: number;
  totalContributions: number;
  totalPayouts: number;
  onTimePaymentRate: number;
  averageRating: number;
  memberSince: string;
  daysActive: number;
  contributionsThisMonth: number;
  payoutsThisMonth: number;
}

export interface Badge {
  id: number;
  name: string;
  description: string;
  icon: string;
  color: string;
  earnedDate: string;
  rarity: 'common' | 'rare' | 'epic' | 'legendary';
}

export interface Activity {
  id: number;
  type: 'dar_joined' | 'dar_created' | 'payment_made' | 'payout_received' | 'badge_earned' | 'trust_score_updated';
  title: string;
  description: string;
  timestamp: string;
  icon?: string;
  relatedEntityType?: string;
  relatedEntityId?: number;
}

export interface TrustScore {
  userId: number;
  score: number;
  rank: number;
  totalUsers: number;
  percentile: number;
  breakdown: TrustScoreBreakdown;
  history: TrustScoreHistory[];
  lastUpdated: string;
}

export interface TrustScoreBreakdown {
  paymentHistory: number;
  darParticipation: number;
  communityReputation: number;
  accountAge: number;
  verificationLevel: number;
  penaltyDeductions: number;
}

export interface TrustScoreHistory {
  date: string;
  score: number;
  change: number;
  reason?: string;
}

export interface TrustRanking {
  userId: number;
  userName: string;
  avatar?: string;
  trustScore: number;
  rank: number;
  badgeCount: number;
  activeDars: number;
  location?: string;
  isFollowing?: boolean;
}

export interface UpdateProfileRequest {
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  avatar?: string;
  bio?: string;
  location?: string;
  dateOfBirth?: string;
  gender?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface UpdateSettingsRequest {
  theme?: 'light' | 'dark' | 'system';
  emailNotifications?: boolean;
  pushNotifications?: boolean;
  smsNotifications?: boolean;
  privacyLevel?: 'public' | 'friends' | 'private';
  showEmail?: boolean;
  showPhone?: boolean;
  showLocation?: boolean;
  currency?: string;
  language?: string;
  timezone?: string;
  dateFormat?: string;
  timeFormat?: '12h' | '24h';
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  pageNumber: number;
  pageSize: number;
  last: boolean;
  first: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/users`;
  private profileApiUrl = `${environment.apiUrl}/profile`;
  private trustApiUrl = `${environment.apiUrl}/trust`;
  private rankingsApiUrl = `${environment.apiUrl}/rankings`;

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  private userProfileSubject = new BehaviorSubject<UserProfile | null>(null);
  public userProfile$ = this.userProfileSubject.asObservable();

  private trustScoreSubject = new BehaviorSubject<TrustScore | null>(null);
  public trustScore$ = this.trustScoreSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Get current user profile
   */
  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`).pipe(
      tap(user => this.currentUserSubject.next(user))
    );
  }

  /**
   * Get full user profile with statistics
   */
  getProfile(userId?: number): Observable<UserProfile> {
    const url = userId
      ? `${this.profileApiUrl}/${userId}`
      : `${this.profileApiUrl}/me`;

    return this.http.get<UserProfile>(url).pipe(
      tap(profile => {
        if (!userId) {
          this.userProfileSubject.next(profile);
          this.currentUserSubject.next(profile.user);
        }
      })
    );
  }

  /**
   * Update user profile
   */
  updateProfile(request: UpdateProfileRequest): Observable<User> {
    return this.http.put<User>(this.profileApiUrl, request).pipe(
      tap(user => this.currentUserSubject.next(user))
    );
  }

  /**
   * Upload profile avatar
   */
  uploadAvatar(file: File): Observable<{ avatarUrl: string }> {
    const formData = new FormData();
    formData.append('avatar', file);

    return this.http.post<{ avatarUrl: string }>(
      `${this.profileApiUrl}/avatar`,
      formData
    ).pipe(
      tap(response => {
        const currentUser = this.currentUserSubject.value;
        if (currentUser) {
          this.currentUserSubject.next({
            ...currentUser,
            avatar: response.avatarUrl
          });
        }
      })
    );
  }

  /**
   * Delete profile avatar
   */
  deleteAvatar(): Observable<void> {
    return this.http.delete<void>(`${this.profileApiUrl}/avatar`).pipe(
      tap(() => {
        const currentUser = this.currentUserSubject.value;
        if (currentUser) {
          this.currentUserSubject.next({
            ...currentUser,
            avatar: undefined
          });
        }
      })
    );
  }

  /**
   * Change password
   */
  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.put<void>(`${this.profileApiUrl}/change-password`, request);
  }

  /**
   * Get user settings
   */
  getSettings(): Observable<UserSettings> {
    return this.http.get<UserSettings>(`${this.profileApiUrl}/settings`);
  }

  /**
   * Update user settings
   */
  updateSettings(request: UpdateSettingsRequest): Observable<UserSettings> {
    return this.http.put<UserSettings>(`${this.profileApiUrl}/settings`, request);
  }

  /**
   * Enable two-factor authentication
   */
  enableTwoFactor(): Observable<{ qrCode: string; secret: string }> {
    return this.http.post<{ qrCode: string; secret: string }>(
      `${this.profileApiUrl}/2fa/enable`,
      {}
    );
  }

  /**
   * Verify and activate two-factor authentication
   */
  verifyTwoFactor(code: string): Observable<{ backupCodes: string[] }> {
    return this.http.post<{ backupCodes: string[] }>(
      `${this.profileApiUrl}/2fa/verify`,
      { code }
    );
  }

  /**
   * Disable two-factor authentication
   */
  disableTwoFactor(password: string): Observable<void> {
    return this.http.post<void>(`${this.profileApiUrl}/2fa/disable`, { password });
  }

  /**
   * Get user's trust score
   */
  getTrustScore(userId?: number): Observable<TrustScore> {
    const url = userId
      ? `${this.trustApiUrl}/${userId}`
      : `${this.trustApiUrl}/me`;

    return this.http.get<TrustScore>(url).pipe(
      tap(trustScore => {
        if (!userId) {
          this.trustScoreSubject.next(trustScore);
        }
      })
    );
  }

  /**
   * Get trust score history
   */
  getTrustScoreHistory(
    userId?: number,
    period: 'week' | 'month' | 'year' | 'all' = 'month'
  ): Observable<TrustScoreHistory[]> {
    const url = userId
      ? `${this.trustApiUrl}/${userId}/history`
      : `${this.trustApiUrl}/me/history`;

    const params = new HttpParams().set('period', period);
    return this.http.get<TrustScoreHistory[]>(url, { params });
  }

  /**
   * Get trust rankings (leaderboard)
   */
  getTrustRankings(
    page: number = 0,
    size: number = 20,
    region?: string,
    timeframe: 'daily' | 'weekly' | 'monthly' | 'all-time' = 'all-time'
  ): Observable<PaginatedResponse<TrustRanking>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('timeframe', timeframe);

    if (region) {
      params = params.set('region', region);
    }

    return this.http.get<PaginatedResponse<TrustRanking>>(this.rankingsApiUrl, { params });
  }

  /**
   * Search users
   */
  searchUsers(
    query: string,
    page: number = 0,
    size: number = 20
  ): Observable<PaginatedResponse<User>> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PaginatedResponse<User>>(`${this.apiUrl}/search`, { params });
  }

  /**
   * Get user by ID
   */
  getUserById(userId: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${userId}`);
  }

  /**
   * Get user statistics
   */
  getUserStatistics(userId?: number): Observable<UserStatistics> {
    const url = userId
      ? `${this.apiUrl}/${userId}/statistics`
      : `${this.apiUrl}/me/statistics`;

    return this.http.get<UserStatistics>(url);
  }

  /**
   * Get user badges
   */
  getUserBadges(userId?: number): Observable<Badge[]> {
    const url = userId
      ? `${this.apiUrl}/${userId}/badges`
      : `${this.apiUrl}/me/badges`;

    return this.http.get<Badge[]>(url);
  }

  /**
   * Get user activity
   */
  getUserActivity(
    userId?: number,
    page: number = 0,
    size: number = 20
  ): Observable<PaginatedResponse<Activity>> {
    const url = userId
      ? `${this.apiUrl}/${userId}/activity`
      : `${this.apiUrl}/me/activity`;

    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PaginatedResponse<Activity>>(url, { params });
  }

  /**
   * Request email verification
   */
  requestEmailVerification(): Observable<void> {
    return this.http.post<void>(`${this.profileApiUrl}/verify-email/request`, {});
  }

  /**
   * Verify email with code
   */
  verifyEmail(code: string): Observable<void> {
    return this.http.post<void>(`${this.profileApiUrl}/verify-email`, { code }).pipe(
      tap(() => {
        const currentUser = this.currentUserSubject.value;
        if (currentUser) {
          this.currentUserSubject.next({
            ...currentUser,
            emailVerified: true
          });
        }
      })
    );
  }

  /**
   * Request phone verification
   */
  requestPhoneVerification(phoneNumber: string): Observable<void> {
    return this.http.post<void>(`${this.profileApiUrl}/verify-phone/request`, { phoneNumber });
  }

  /**
   * Verify phone with code
   */
  verifyPhone(code: string): Observable<void> {
    return this.http.post<void>(`${this.profileApiUrl}/verify-phone`, { code }).pipe(
      tap(() => {
        const currentUser = this.currentUserSubject.value;
        if (currentUser) {
          this.currentUserSubject.next({
            ...currentUser,
            phoneVerified: true
          });
        }
      })
    );
  }

  /**
   * Start KYC verification process
   */
  startKYCVerification(documents: any): Observable<{ verificationId: string }> {
    return this.http.post<{ verificationId: string }>(
      `${this.profileApiUrl}/kyc/start`,
      documents
    );
  }

  /**
   * Get KYC verification status
   */
  getKYCStatus(): Observable<{ status: string; message?: string }> {
    return this.http.get<{ status: string; message?: string }>(
      `${this.profileApiUrl}/kyc/status`
    );
  }

  /**
   * Deactivate account
   */
  deactivateAccount(password: string, reason?: string): Observable<void> {
    return this.http.post<void>(`${this.profileApiUrl}/deactivate`, {
      password,
      reason
    });
  }

  /**
   * Delete account permanently
   */
  deleteAccount(password: string, reason?: string): Observable<void> {
    return this.http.delete<void>(`${this.profileApiUrl}/delete`, {
      body: { password, reason }
    });
  }

  /**
   * Report user
   */
  reportUser(userId: number, reason: string, details?: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${userId}/report`, {
      reason,
      details
    });
  }

  /**
   * Block user
   */
  blockUser(userId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${userId}/block`, {});
  }

  /**
   * Unblock user
   */
  unblockUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${userId}/block`);
  }

  /**
   * Get blocked users
   */
  getBlockedUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/me/blocked`);
  }

  /**
   * Follow user
   */
  followUser(userId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${userId}/follow`, {});
  }

  /**
   * Unfollow user
   */
  unfollowUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${userId}/follow`);
  }

  /**
   * Get followers
   */
  getFollowers(
    userId?: number,
    page: number = 0,
    size: number = 20
  ): Observable<PaginatedResponse<User>> {
    const url = userId
      ? `${this.apiUrl}/${userId}/followers`
      : `${this.apiUrl}/me/followers`;

    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PaginatedResponse<User>>(url, { params });
  }

  /**
   * Get following
   */
  getFollowing(
    userId?: number,
    page: number = 0,
    size: number = 20
  ): Observable<PaginatedResponse<User>> {
    const url = userId
      ? `${this.apiUrl}/${userId}/following`
      : `${this.apiUrl}/me/following`;

    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PaginatedResponse<User>>(url, { params });
  }

  /**
   * Export user data (GDPR)
   */
  exportUserData(): Observable<Blob> {
    return this.http.get(`${this.profileApiUrl}/export`, {
      responseType: 'blob'
    });
  }

  /**
   * Clear cached data
   */
  clearCache(): void {
    this.currentUserSubject.next(null);
    this.userProfileSubject.next(null);
    this.trustScoreSubject.next(null);
  }

  /**
   * Update cached user
   */
  updateCachedUser(user: User): void {
    this.currentUserSubject.next(user);
  }
}
