import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

/**
 * Loading Service
 *
 * Manages loading states throughout the application.
 * Tracks global and individual request loading states.
 *
 * Key Features:
 * - Global loading state management
 * - Individual request tracking
 * - Progress percentage tracking
 * - Observable-based state updates
 */
@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  private loadingSubject = new BehaviorSubject<boolean>(false);
  private progressSubject = new BehaviorSubject<number>(0);
  private activeRequests = new Set<string>();

  /**
   * Observable stream of loading state
   */
  public loading$: Observable<boolean> = this.loadingSubject.asObservable();

  /**
   * Observable stream of loading progress (0-100)
   */
  public progress$: Observable<number> = this.progressSubject.asObservable();

  constructor() {}

  /**
   * Show loading indicator
   */
  show(): void {
    this.loadingSubject.next(true);
  }

  /**
   * Hide loading indicator
   */
  hide(): void {
    this.loadingSubject.next(false);
    this.progressSubject.next(0);
  }

  /**
   * Get current loading state (synchronous)
   */
  isLoading(): boolean {
    return this.loadingSubject.value;
  }

  /**
   * Set loading progress percentage
   */
  setProgress(progress: number): void {
    const clampedProgress = Math.max(0, Math.min(100, progress));
    this.progressSubject.next(clampedProgress);
  }

  /**
   * Get current progress percentage (synchronous)
   */
  getProgress(): number {
    return this.progressSubject.value;
  }

  /**
   * Add a request to tracking
   */
  addRequest(requestId: string): void {
    this.activeRequests.add(requestId);
    if (this.activeRequests.size > 0) {
      this.show();
    }
  }

  /**
   * Remove a request from tracking
   */
  removeRequest(requestId: string): void {
    this.activeRequests.delete(requestId);
    if (this.activeRequests.size === 0) {
      this.hide();
    }
  }

  /**
   * Get number of active requests
   */
  getActiveRequestCount(): number {
    return this.activeRequests.size;
  }

  /**
   * Check if a specific request is active
   */
  isRequestActive(requestId: string): boolean {
    return this.activeRequests.has(requestId);
  }

  /**
   * Clear all active requests
   */
  clearAllRequests(): void {
    this.activeRequests.clear();
    this.hide();
  }

  /**
   * Show loading with automatic timeout
   */
  showWithTimeout(timeout: number = 30000): void {
    this.show();
    setTimeout(() => {
      if (this.isLoading()) {
        this.hide();
        console.warn('Loading timeout reached, hiding indicator');
      }
    }, timeout);
  }
}
