// HTTP クライアント (fetch ラッパー)
//
// vite.config.ts の proxy 設定により、相対パスで Spring Boot にリクエストが届く。
//   FE: fetch('/books')  →  Vite proxy  →  http://localhost:8080/books
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
  get:    <T>(path: string)               => request<T>(path),
  post:   <T>(path: string, body: unknown) => request<T>(path, { method: 'POST',   body: JSON.stringify(body) }),
  put:    <T>(path: string, body: unknown) => request<T>(path, { method: 'PUT',    body: JSON.stringify(body) }),
  delete: <T>(path: string)               => request<T>(path, { method: 'DELETE' }),
}
