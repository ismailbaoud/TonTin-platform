/**
 * Generic paginated response wrapper for API responses
 *
 * This interface represents the structure of paginated responses from the backend API.
 * It includes the data content along with pagination metadata.
 *
 * @template T - The type of items in the content array
 */
export interface PaginatedResponse<T> {
  /** Array of items for the current page */
  content: T[];

  /** Total number of elements across all pages */
  totalElements: number;

  /** Total number of pages available */
  totalPages: number;

  /** Current page number (0-based) */
  pageNumber: number;

  /** Number of items per page */
  pageSize: number;

  /** Whether this is the last page */
  last: boolean;

  /** Whether this is the first page */
  first: boolean;

  /** Whether the page is empty (no content) */
  empty?: boolean;

  /** Number of elements in the current page */
  numberOfElements?: number;
}

/**
 * Helper function to check if there are more pages available
 */
export function hasMorePages<T>(response: PaginatedResponse<T>): boolean {
  return !response.last;
}

/**
 * Helper function to get the next page number
 */
export function getNextPage<T>(response: PaginatedResponse<T>): number | null {
  return hasMorePages(response) ? response.pageNumber + 1 : null;
}

/**
 * Helper function to get the previous page number
 */
export function getPreviousPage<T>(response: PaginatedResponse<T>): number | null {
  return !response.first ? response.pageNumber - 1 : null;
}

/**
 * Helper function to calculate the range of items displayed
 */
export function getItemRange<T>(response: PaginatedResponse<T>): { start: number; end: number } {
  const start = response.pageNumber * response.pageSize + 1;
  const end = Math.min(start + response.content.length - 1, response.totalElements);
  return { start, end };
}

/**
 * Helper function to create an empty paginated response
 */
export function createEmptyPaginatedResponse<T>(): PaginatedResponse<T> {
  return {
    content: [],
    totalElements: 0,
    totalPages: 0,
    pageNumber: 0,
    pageSize: 20,
    last: true,
    first: true,
    empty: true,
    numberOfElements: 0
  };
}
