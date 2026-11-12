import { describe, it, expect } from 'vitest';
import { toLocalDateTimeInputValue, formatLocalTime, formatLocalDateTime } from '../utils/datetime';

describe('datetime utilities', () => {
  it('formats local Date into YYYY-MM-DDTHH:mm without UTC drift', () => {
    const localDate = new Date(2026, 4, 15, 19, 30, 0);
    expect(toLocalDateTimeInputValue(localDate)).toBe('2026-05-15T19:30');
  });

  it('pads single-digit months, days, hours, and minutes correctly', () => {
    const localDate = new Date(2026, 0, 5, 7, 4, 0);
    expect(toLocalDateTimeInputValue(localDate)).toBe('2026-01-05T07:04');
  });

  it('handles invalid inputs gracefully', () => {
    expect(toLocalDateTimeInputValue('invalid-date')).toBe('');
    expect(formatLocalTime(undefined)).toBe('ASAP');
    expect(formatLocalTime('not-a-date')).toBe('ASAP');
    expect(formatLocalDateTime(undefined)).toBe('N/A');
  });
});

