import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { catchError, map } from "rxjs/operators";
import { of } from "rxjs";
import { environment } from "../../../../../../environments/environment";
import { Dar } from "../../dars/models";
import { CreateDarRequest } from "../../dars/models";

export interface AdminUser {
  id: string;
  userName: string;
  email: string;
  role: string | null;
  status: string | null;
  avatar: string | null;
}

export interface AdminPageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface AdminCreateDarRequest {
  organizerUserId: string;
  dart: CreateDarRequest;
}

@Injectable({
  providedIn: "root",
})
export class AdminService {
  private usersApi = `${environment.apiUrl}/v1/user/admin/list`;
  private darsApi = `${environment.apiUrl}/v1/dart/admin/all`;
  private createAdminDarApi = `${environment.apiUrl}/v1/dart/admin/create`;

  constructor(private http: HttpClient) {}

  getAdminUsers(
    page: number = 0,
    size: number = 20,
    query?: string,
    status?: string,
  ): Observable<AdminPageResponse<AdminUser>> {
    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());
    if (query) {
      params = params.set("query", query);
    }
    if (status) {
      params = params.set("status", status);
    }
    return this.http.get<AdminPageResponse<AdminUser>>(this.usersApi, { params }).pipe(
      catchError(() =>
        // Fallback when backend admin endpoint is not yet deployed/running.
        this.http
          .get<AdminUser[]>(
            `${environment.apiUrl}/v1/user/search`,
            { params: new HttpParams().set("username", query || "") },
          )
          .pipe(
            map((users) => ({
              content: users.map((u) => ({
                ...u,
                role: null,
                status: null,
              })),
              totalElements: users.length,
              totalPages: 1,
              page: 0,
              size: users.length || size,
              first: true,
              last: true,
            })),
            catchError(() =>
              of({
                content: [],
                totalElements: 0,
                totalPages: 0,
                page: 0,
                size,
                first: true,
                last: true,
              }),
            ),
          ),
      ),
    );
  }

  getAdminDars(
    page: number = 0,
    size: number = 20,
    status?: string,
  ): Observable<AdminPageResponse<Dar>> {
    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());
    if (status) {
      params = params.set("status", status);
    }
    return this.http.get<AdminPageResponse<Dar>>(this.darsApi, { params }).pipe(
      catchError(() =>
        // Fallback to user-scoped endpoint if admin endpoint is unavailable.
        this.http
          .get<any>(`${environment.apiUrl}/v1/dart/my-dars`, { params })
          .pipe(
            map((res) => ({
              content: res?.content || [],
              totalElements: res?.totalElements || 0,
              totalPages: res?.totalPages || 0,
              page: res?.page ?? 0,
              size: res?.size ?? size,
              first: res?.first ?? true,
              last: res?.last ?? true,
            })),
            catchError(() =>
              of({
                content: [],
                totalElements: 0,
                totalPages: 0,
                page: 0,
                size,
                first: true,
                last: true,
              }),
            ),
          ),
      ),
    );
  }

  createAdminDar(payload: AdminCreateDarRequest): Observable<Dar> {
    return this.http.post<Dar>(this.createAdminDarApi, payload);
  }

  updateUserStatus(userId: string, status: string): Observable<AdminUser> {
    const params = new HttpParams().set("status", status);
    return this.http.put<AdminUser>(`${environment.apiUrl}/v1/user/admin/${userId}/status`, null, { params });
  }

  updateUserRole(userId: string, role: string): Observable<AdminUser> {
    const params = new HttpParams().set("role", role);
    return this.http.put<AdminUser>(`${environment.apiUrl}/v1/user/admin/${userId}/role`, null, { params });
  }

  deleteUser(userId: string): Observable<AdminUser> {
    return this.http.delete<AdminUser>(`${environment.apiUrl}/v1/user/admin/${userId}`);
  }
}

