// HTTP クライアント (fetch ラッパー)
//
// vite.config.ts の proxy 設定により、相対パスで Spring Boot にリクエストが届く。
//   FE: fetch('/api/books')  →  Vite proxy  →  http://localhost:8080/api/books
// CORS の設定は不要。新しいエンドポイントは vite.config.ts の proxy にも追記すること。

const BASE_URL = ''

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json', ...init?.headers },
    ...init,
  })
  if (!res.ok) {
    throw new Error(`API error: ${res.status} ${res.statusText}`)
  }
  return res.json() as Promise<T>
}

export const api = {
  get:    <T>(path: string)                => request<T>(path),
  post:   <T>(path: string, body: unknown) => request<T>(path, { method: 'POST',   body: JSON.stringify(body) }),
  put:    <T>(path: string, body: unknown) => request<T>(path, { method: 'PUT',    body: JSON.stringify(body) }),
  delete: <T>(path: string)               => request<T>(path, { method: 'DELETE' }),
}

// ─── 型定義 ────────────────────────────────────────────────────────────────────

export type CopyStatus = 'AVAILABLE' | 'CHECKED_OUT' | 'REPAIR' | 'DISCARDED'

export interface BookCopy {
  id: number
  copyCode: string
  status: CopyStatus
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

// ─── 図書 API ──────────────────────────────────────────────────────────────────

export async function fetchBooks(q?: string): Promise<Book[] | null> {
  try {
    const path = q ? `/api/books?q=${encodeURIComponent(q)}` : '/api/books'
    return await api.get<Book[]>(path)
  } catch {
    return null
  }
}

export async function createBook(body: BookRequest): Promise<Book | null> {
  try {
    return await api.post<Book>('/api/books', body)
  } catch {
    return null
  }
}

export async function deleteBook(id: number): Promise<boolean> {
  try {
    await api.delete<void>(`/api/books/${id}`)
    return true
  } catch {
    return false
  }
}
