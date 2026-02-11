/**
 * Contribution payment frequency enumeration
 *
 * Defines how often members are expected to make contributions to the Dâr.
 */
export enum DarFrequency {
  /** Contributions are made every week */
  WEEKLY = 'weekly',

  /** Contributions are made every two weeks */
  BI_WEEKLY = 'bi-weekly',

  /** Contributions are made every month */
  MONTHLY = 'monthly',

  /** Contributions are made every three months */
  QUARTERLY = 'quarterly'
}

/**
 * Helper function to get user-friendly frequency label
 */
export function getDarFrequencyLabel(frequency: DarFrequency): string {
  const labels: Record<DarFrequency, string> = {
    [DarFrequency.WEEKLY]: 'Weekly',
    [DarFrequency.BI_WEEKLY]: 'Bi-Weekly',
    [DarFrequency.MONTHLY]: 'Monthly',
    [DarFrequency.QUARTERLY]: 'Quarterly'
  };
  return labels[frequency];
}

/**
 * Helper function to calculate number of days between contributions
 */
export function getDarFrequencyDays(frequency: DarFrequency): number {
  const days: Record<DarFrequency, number> = {
    [DarFrequency.WEEKLY]: 7,
    [DarFrequency.BI_WEEKLY]: 14,
    [DarFrequency.MONTHLY]: 30,
    [DarFrequency.QUARTERLY]: 90
  };
  return days[frequency];
}

/**
 * Helper function to get next contribution date
 */
export function getNextContributionDate(
  currentDate: Date,
  frequency: DarFrequency
): Date {
  const nextDate = new Date(currentDate);
  const daysToAdd = getDarFrequencyDays(frequency);
  nextDate.setDate(nextDate.getDate() + daysToAdd);
  return nextDate;
}
