import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../../environments/environment';
import { Round, RoundStatistics, PaginatedResponse } from '../models';

@Injectable({
  providedIn: 'root'
})
export class RoundService {
  private apiUrl = `${environment.apiUrl}/v1/rounds`;

  constructor(private http: HttpClient) {}

  /**
   * Get all rounds for a dart
   */
  getRoundsByDartId(dartId: string): Observable<Round[]> {
    return this.http.get<Round[]>(`${this.apiUrl}/dart/${dartId}`);
  }

  /**
   * Get rounds for a dart with pagination
   */
  getRoundsByDartIdPaginated(
    dartId: string,
    page: number = 0,
    size: number = 10
  ): Observable<PaginatedResponse<Round>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PaginatedResponse<Round>>(
      `${this.apiUrl}/dart/${dartId}/paginated`,
      { params }
    );
  }

  /**
   * Get a round by ID
   */
  getRoundById(dartId: string, roundId: string): Observable<Round> {
    return this.http.get<Round>(`${this.apiUrl}/dart/${dartId}/${roundId}`);
  }

  /**
   * Get the current (next unpaid) round for a dart
   */
  getCurrentRound(dartId: string): Observable<Round> {
    return this.http.get<Round>(`${this.apiUrl}/dart/${dartId}/current`);
  }

  /**
   * Get round statistics for a dart
   */
  getRoundStatistics(dartId: string): Observable<RoundStatistics> {
    return this.http.get<RoundStatistics>(`${this.apiUrl}/dart/${dartId}/statistics`);
  }

  /**
   * Mark a round as paid
   */
  markRoundAsPaid(dartId: string, roundId: string): Observable<Round> {
    return this.http.put<Round>(
      `${this.apiUrl}/dart/${dartId}/${roundId}/mark-paid`,
      {}
    );
  }
}
