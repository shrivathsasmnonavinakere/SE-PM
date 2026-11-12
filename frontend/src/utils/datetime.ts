/**
 * Formats a Date or ISO timestamp into YYYY-MM-DDTHH:mm in the browser's LOCAL timezone,
 * preventing the classic UTC offset shift when populating <input type="datetime-local">.
 */
export function toLocalDateTimeInputValue(dateInput: Date | string | number): string {
  const date = dateInput instanceof Date ? dateInput : new Date(dateInput);
  if (Number.isNaN(date.getTime())) {
    return '';
  }
  const pad = (n: number) => String(n).padStart(2, '0');
  const year = date.getFullYear();
  const month = pad(date.getMonth() + 1);
  const day = pad(date.getDate());
  const hours = pad(date.getHours());
  const minutes = pad(date.getMinutes());
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

export function formatLocalTime(dateInput?: string | Date): string {
  if (!dateInput) return 'ASAP';
  const date = new Date(dateInput);
  if (Number.isNaN(date.getTime())) return 'ASAP';
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

export function formatLocalDateTime(dateInput?: string | Date): string {
  if (!dateInput) return 'N/A';
  const date = new Date(dateInput);
  if (Number.isNaN(date.getTime())) return 'N/A';
  return date.toLocaleString([], {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

