import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable, BehaviorSubject, tap } from "rxjs";
import { environment } from "../../../../../../environments/environment";

// Import models
import {
  Dar,
  DarDetails,
  Member,
  Tour,
  Transaction,
  Message,
  CreateDarRequest,
  UpdateDarRequest,
  InviteMemberRequest,
  JoinDarRequest,
  MemberOrder,
  GenerateInviteCodeResponse,
  PaginatedResponse,
} from "../models";

// Import constants
import { DAR_PAGINATION } from "../constants";

// Re-export models for backward compatibility
export type {
  Dar,
  DarDetails,
  Member,
  Tour,
  Transaction,
  Message,
  CreateDarRequest,
  UpdateDarRequest,
  InviteMemberRequest,
  JoinDarRequest,
  MemberOrder,
  GenerateInviteCodeResponse,
  PaginatedResponse,
} from "../models";

// Re-export enums for backward compatibility
export {
  DarStatus,
  DarFrequency,
  PaymentStatus,
  MemberRole,
  getDarStatusLabel,
  getDarStatusColor,
  getDarFrequencyLabel,
  getDarFrequencyDays,
  getNextContributionDate,
  getPaymentStatusLabel,
  getPaymentStatusColor,
  getPaymentStatusIcon,
  isPaymentActionRequired,
  isPaymentComplete,
  hasPaymentIssue,
  getMemberRoleLabel,
  getMemberRoleBadgeColor,
  hasAdminPrivileges,
  canManageMembers,
  canEditDarSettings,
  canDeleteDar,
  canInviteMembers,
  canSendMessages,
  getRolePermissions,
} from "../enums";

// Re-export constants for backward compatibility
export { DAR_CONFIG, DAR_MESSAGES, DAR_PAGINATION } from "../constants";

@Injectable({
  providedIn: "root",
})
export class DarService {
  private apiUrl = `${environment.apiUrl}/dars`;
  private darsSubject = new BehaviorSubject<Dar[]>([]);
  public dars$ = this.darsSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Get all Dârs for the current user
   */
  getMyDars(
    status?: string,
    page: number = 0,
    size: number = DAR_PAGINATION.DEFAULT_PAGE_SIZE,
  ): Observable<PaginatedResponse<Dar>> {
    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());

    if (status) {
      params = params.set("status", status);
    }

    return this.http
      .get<PaginatedResponse<Dar>>(`${this.apiUrl}/my-dars`, { params })
      .pipe(
        tap((response) => {
          if (page === 0) {
            this.darsSubject.next(response.content);
          }
        }),
      );
  }

  /**
   * Get Dâr details by ID
   */
  getDarDetails(darId: number): Observable<DarDetails> {
    return this.http.get<DarDetails>(`${this.apiUrl}/${darId}`);
  }

  /**
   * Create a new Dâr
   */
  createDar(request: CreateDarRequest): Observable<Dar> {
    return this.http.post<Dar>(this.apiUrl, request).pipe(
      tap((dar) => {
        const currentDars = this.darsSubject.value;
        this.darsSubject.next([dar, ...currentDars]);
      }),
    );
  }

  /**
   * Update Dâr details (organizer only)
   */
  updateDar(darId: number, request: UpdateDarRequest): Observable<Dar> {
    return this.http.put<Dar>(`${this.apiUrl}/${darId}`, request);
  }

  /**
   * Delete/Cancel a Dâr (organizer only)
   */
  deleteDar(darId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${darId}`).pipe(
      tap(() => {
        const currentDars = this.darsSubject.value;
        this.darsSubject.next(currentDars.filter((d) => d.id !== darId));
      }),
    );
  }

  /**
   * Join a Dâr
   */
  joinDar(request: JoinDarRequest): Observable<Dar> {
    return this.http.post<Dar>(`${this.apiUrl}/join`, request).pipe(
      tap((dar) => {
        const currentDars = this.darsSubject.value;
        this.darsSubject.next([dar, ...currentDars]);
      }),
    );
  }

  /**
   * Leave a Dâr
   */
  leaveDar(darId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${darId}/leave`, {}).pipe(
      tap(() => {
        const currentDars = this.darsSubject.value;
        this.darsSubject.next(currentDars.filter((d) => d.id !== darId));
      }),
    );
  }

  /**
   * Invite member to Dâr (organizer only)
   */
  inviteMember(request: InviteMemberRequest): Observable<void> {
    return this.http.post<void>(
      `${this.apiUrl}/${request.darId}/invite`,
      request,
    );
  }

  /**
   * Remove member from Dâr (organizer only)
   */
  removeMember(darId: number, memberId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${darId}/members/${memberId}`,
    );
  }

  /**
   * Get Dâr members
   */
  getMembers(darId: number): Observable<Member[]> {
    return this.http.get<Member[]>(`${this.apiUrl}/${darId}/members`);
  }

  /**
   * Get Dâr tours (payment schedule)
   */
  getTours(darId: number): Observable<Tour[]> {
    return this.http.get<Tour[]>(`${this.apiUrl}/${darId}/tours`);
  }

  /**
   * Get Dâr transactions
   */
  getTransactions(
    darId: number,
    page: number = 0,
    size: number = DAR_PAGINATION.TRANSACTIONS_PAGE_SIZE,
  ): Observable<PaginatedResponse<Transaction>> {
    const params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());

    return this.http.get<PaginatedResponse<Transaction>>(
      `${this.apiUrl}/${darId}/transactions`,
      { params },
    );
  }

  /**
   * Get Dâr messages
   */
  getMessages(
    darId: number,
    page: number = 0,
    size: number = DAR_PAGINATION.MESSAGES_PAGE_SIZE,
  ): Observable<PaginatedResponse<Message>> {
    const params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());

    return this.http.get<PaginatedResponse<Message>>(
      `${this.apiUrl}/${darId}/messages`,
      { params },
    );
  }

  /**
   * Send message to Dâr
   */
  sendMessage(darId: number, content: string): Observable<Message> {
    return this.http.post<Message>(`${this.apiUrl}/${darId}/messages`, {
      content,
    });
  }

  /**
   * Generate new invite code (organizer only)
   */
  generateInviteCode(darId: number): Observable<GenerateInviteCodeResponse> {
    return this.http.post<GenerateInviteCodeResponse>(
      `${this.apiUrl}/${darId}/generate-invite-code`,
      {},
    );
  }

  /**
   * Get public Dârs (for discovery)
   */
  getPublicDars(
    page: number = 0,
    size: number = DAR_PAGINATION.DEFAULT_PAGE_SIZE,
    searchQuery?: string,
  ): Observable<PaginatedResponse<Dar>> {
    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());

    if (searchQuery) {
      params = params.set("search", searchQuery);
    }

    return this.http.get<PaginatedResponse<Dar>>(`${this.apiUrl}/public`, {
      params,
    });
  }

  /**
   * Start a Dâr (organizer only, when minimum members reached)
   */
  startDar(darId: number): Observable<Dar> {
    return this.http.post<Dar>(`${this.apiUrl}/${darId}/start`, {});
  }

  /**
   * Complete a tour/cycle
   */
  completeTour(darId: number, tourId: number): Observable<Tour> {
    return this.http.post<Tour>(
      `${this.apiUrl}/${darId}/tours/${tourId}/complete`,
      {},
    );
  }

  /**
   * Report a member (for trust score issues)
   */
  reportMember(
    darId: number,
    memberId: number,
    reason: string,
  ): Observable<void> {
    return this.http.post<void>(
      `${this.apiUrl}/${darId}/members/${memberId}/report`,
      {
        reason,
      },
    );
  }

  /**
   * Get Dâr statistics (for organizer)
   */
  getDarStats(darId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${darId}/stats`);
  }

  /**
   * Update member turn order (organizer only)
   */
  updateTurnOrder(darId: number, memberOrder: MemberOrder[]): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${darId}/turn-order`, {
      memberOrder,
    });
  }

  /**
   * Clear cached Dârs
   */
  clearCache(): void {
    this.darsSubject.next([]);
  }
}
