const BASE_URL = ''

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json', ...init?.headers },
    ...init,
  })
  if (!res.ok) {
    const text = await res.text().catch(() => '')
    throw new Error(text || `API error: ${res.status}`)
  }
  if (res.status === 204) return undefined as T
  return res.json() as Promise<T>
}

export const api = {
  get:    <T>(path: string)                => request<T>(path),
  post:   <T>(path: string, body: unknown) => request<T>(path, { method: 'POST',   body: JSON.stringify(body) }),
  put:    <T>(path: string, body: unknown) => request<T>(path, { method: 'PUT',    body: JSON.stringify(body) }),
  patch:  <T>(path: string, body: unknown) => request<T>(path, { method: 'PATCH',  body: JSON.stringify(body) }),
  delete: <T>(path: string)               => request<T>(path, { method: 'DELETE' }),
}

// ─── 図書 ────────────────────────────────────────────────────────────────────

export type CopyStatus = 'AVAILABLE' | 'CHECKED_OUT' | 'REPAIR' | 'DISCARDED'

export interface BookCopy {
  id: number
  copyCode: string
  status: CopyStatus
  dueDate: string | null
}

export interface Book {
  id: number
  isbn: string
  title: string
  author: string
  publisher: string
  publishedYear: number
  callNumber: string
  copies: BookCopy[]
}

export interface BookRequest {
  isbn: string
  title: string
  author: string
  publisher: string
  publishedYear: number
  callNumber: string
}

export const fetchBooks   = (q?: string) =>
  api.get<Book[]>(q ? `/api/books?q=${encodeURIComponent(q)}` : '/api/books')
export const createBook   = (body: BookRequest) => api.post<Book>('/api/books', body)
export const deleteBook   = (id: number) => api.delete<void>(`/api/books/${id}`)

// ─── 利用者 ──────────────────────────────────────────────────────────────────

export interface Member {
  id: number
  memberNumber: string
  name: string
  email: string
  phone: string
  active: boolean
}

export interface MemberRequest {
  memberNumber: string
  name: string
  email: string
  phone: string
}

export const fetchMembers    = () => api.get<Member[]>('/api/members')
export const createMember    = (body: MemberRequest) => api.post<Member>('/api/members', body)
export const deactivateMember = (id: number) => api.delete<void>(`/api/members/${id}`)

// ─── 貸出 ────────────────────────────────────────────────────────────────────

export interface Loan {
  id: number
  memberId: number
  memberName: string
  bookCopyId: number
  copyCode: string
  bookTitle: string
  loanDate: string
  dueDate: string
  returnedDate: string | null
}

export interface LoanRequest {
  memberId: number
  bookCopyId: number
}

export const fetchLoansByMember = (memberId: number) =>
  api.get<Loan[]>(`/api/loans?memberId=${memberId}`)
export const checkout   = (body: LoanRequest) => api.post<Loan>('/api/loans', body)
export const returnBook = (loanId: number)    => api.post<Loan>(`/api/loans/${loanId}/return`, {})

// ─── 貸出ルール ───────────────────────────────────────────────────────────────

export interface LoanRule {
  id: number
  maxLoanCount: number
  loanPeriodDays: number
}

export interface LoanRuleRequest {
  maxLoanCount: number
  loanPeriodDays: number
}

export const fetchLoanRule  = () => api.get<LoanRule>('/api/loan-rules/1')
export const updateLoanRule = (body: LoanRuleRequest) => api.put<LoanRule>('/api/loan-rules/1', body)

// ─── 統計 ────────────────────────────────────────────────────────────────────

export interface StatsResponse {
  totalBooks: number
  totalMembers: number
  activeLoans: number
  overdueLoans: number
  loansByMonth: { month: string; count: number }[]
  topGenres: { genre: string; count: number }[]
}

export const fetchStats = () => api.get<StatsResponse>('/api/stats')

// ─── 予約 ────────────────────────────────────────────────────────────────────

export type ReservationStatus = 'WAITING' | 'READY' | 'CANCELLED' | 'FULFILLED'

export interface Reservation {
  id: number
  bookId: number
  bookTitle: string
  memberId: number
  memberName: string
  reservedAt: string
  status: ReservationStatus
}

export const fetchReservationsByMember = (memberId: number) =>
  api.get<Reservation[]>(`/api/reservations?memberId=${memberId}`)
export const fetchReservationsByBook = (bookId: number) =>
  api.get<Reservation[]>(`/api/reservations?bookId=${bookId}`)
export const createReservation = (bookId: number, memberId: number) =>
  api.post<Reservation>('/api/reservations', { bookId, memberId })
export const cancelReservation = (reservationId: number, memberId: number) =>
  api.delete<void>(`/api/reservations/${reservationId}?memberId=${memberId}`)

// ─── 監査ログ ─────────────────────────────────────────────────────────────────

export interface AuditLog {
  id: number
  action: string
  targetType: string
  targetId: string | null
  performedBy: string
  performedAt: string
}

export const fetchAuditLogs = (user?: string) =>
  api.get<AuditLog[]>(user ? `/api/audit-logs?user=${encodeURIComponent(user)}` : '/api/audit-logs')

// ─── 延滞管理 ─────────────────────────────────────────────────────────────────

export interface OverdueItem {
  loanId: number
  memberId: number
  memberName: string
  memberEmail: string
  bookTitle: string
  copyCode: string
  dueDate: string
  overdueDays: number
}

export const fetchOverdue    = () => api.get<OverdueItem[]>('/api/overdue')
export const sendReminders   = () => api.post<OverdueItem[]>('/api/overdue/notify', {})
