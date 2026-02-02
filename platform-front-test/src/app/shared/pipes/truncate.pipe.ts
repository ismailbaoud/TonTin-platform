import { Pipe, PipeTransform } from '@angular/core';

/**
 * Truncate Pipe
 *
 * Truncates text to a specified length and adds ellipsis.
 *
 * Usage:
 *   {{ longText | truncate:50 }}
 *   {{ longText | truncate:100:'...' }}
 *   {{ longText | truncate:75:'... Read more' }}
 */
@Pipe({
  name: 'truncate',
  standalone: true
})
export class TruncatePipe implements PipeTransform {
  /**
   * Transform text by truncating it to specified length
   *
   * @param value - The text to truncate
   * @param limit - Maximum length (default: 50)
   * @param ellipsis - String to append (default: '...')
   * @returns Truncated text
   */
  transform(
    value: string | null | undefined,
    limit: number = 50,
    ellipsis: string = '...'
  ): string {
    // Handle null or undefined
    if (!value) {
      return '';
    }

    // Convert to string if needed
    const text = String(value);

    // Return original if shorter than limit
    if (text.length <= limit) {
      return text;
    }

    // Truncate and add ellipsis
    return text.substring(0, limit).trim() + ellipsis;
  }
}
