import { useState } from 'react'
import {
  fetchMembers, fetchReservationsByMember, createReservation, cancelReservation,
  fetchBooks, type Member, type Reservation, type Book,
} from '../api/client'
import styles from './ReservationPage.module.css'

const STATUS_LABEL: Record<string, string> = {
  WAITING:   '予約待ち',
  READY:     '貸出可能',
  CANCELLED: 'キャンセル済み',
  FULFILLED: '貸出済み',
}
const STATUS_CLASS: Record<string, string> = {
  WAITING:   styles.waiting,
  READY:     styles.ready,
  CANCELLED: styles.cancelled,
  FULFILLED: styles.fulfilled,
}

export default function ReservationPage() {
  const [members, setMembers] = useState<Member[]>([])
  const [books, setBooks]     = useState<Book[]>([])
  const [selectedMember, setSelectedMember] = useState<Member | null>(null)
  const [reservations, setReservations]     = useState<Reservation[]>([])
  const [bookQuery, setBookQuery] = useState('')
  const [message, setMessage]    = useState('')
  const [error, setError]        = useState('')

  const loadMembers = async () => {
    if (members.length > 0) return
    const data = await fetchMembers().catch(() => [])
    setMembers(data.filter((m) => m.active))
  }

  const selectMember = async (m: Member) => {
    setSelectedMember(m)
    setMessage('')
    setError('')
    const data = await fetchReservationsByMember(m.id).catch(() => [])
    setReservations(data)
  }

  const searchBooks = async (e: React.FormEvent) => {
    e.preventDefault()
    const data = await fetchBooks(bookQuery || undefined).catch(() => [])
    setBooks(data)
  }

  const handleReserve = async (book: Book) => {
    if (!selectedMember) { setError('先に利用者を選択してください'); return }
    setMessage('')
    setError('')
    try {
      await createReservation(book.id, selectedMember.id)
      setMessage(`「${book.title}」を予約しました`)
      const data = await fetchReservationsByMember(selectedMember.id)
      setReservations(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : '予約に失敗しました')
    }
  }

  const handleCancel = async (r: Reservation) => {
    if (!selectedMember) return
    if (!confirm(`「${r.bookTitle}」の予約をキャンセルしますか？`)) return
    try {
      await cancelReservation(r.id, selectedMember.id)
      setReservations((prev) =>
        prev.map((x) => x.id === r.id ? { ...x, status: 'CANCELLED' as const } : x))
      setMessage(`「${r.bookTitle}」の予約をキャンセルしました`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'キャンセルに失敗しました')
    }
  }

  const activeReservations = reservations.filter(
    (r) => r.status === 'WAITING' || r.status === 'READY')

  return (
    <div className={styles.page}>
      <h3>予約管理</h3>

      {/* 利用者選択 */}
      <div className={styles.row}>
        <label>利用者</label>
        <select
          value={selectedMember?.id ?? ''}
          onClick={loadMembers}
          onChange={(e) => {
            const m = members.find((x) => x.id === Number(e.target.value))
            if (m) selectMember(m)
          }}
        >
          <option value="">-- 利用者を選択 --</option>
          {members.map((m) => (
            <option key={m.id} value={m.id}>{m.memberNumber} {m.name}</option>
          ))}
        </select>
      </div>

      {message && <p className={styles.success}>{message}</p>}
      {error   && <p className={styles.error}>{error}</p>}

      <div className={styles.columns}>
        {/* 図書検索・予約 */}
        <section>
          <h4>図書を検索して予約</h4>
          <form className={styles.searchForm} onSubmit={searchBooks}>
            <input
              placeholder="タイトル・著者で検索..."
              value={bookQuery}
              onChange={(e) => setBookQuery(e.target.value)}
            />
            <button type="submit">検索</button>
          </form>
          {books.length > 0 && (
            <table className={styles.table}>
              <thead>
                <tr><th>タイトル</th><th>著者</th><th>在庫</th><th></th></tr>
              </thead>
              <tbody>
                {books.map((b) => {
                  const available = b.copies.filter((c) => c.status === 'AVAILABLE').length
                  return (
                    <tr key={b.id}>
                      <td>{b.title}</td>
                      <td>{b.author}</td>
                      <td className={available > 0 ? styles.available : styles.checkedOut}>
                        {available > 0 ? `在庫${available}冊` : '貸出中'}
                      </td>
                      <td>
                        <button
                          className={styles.reserveBtn}
                          onClick={() => handleReserve(b)}
                          disabled={!selectedMember}
                        >
                          予約
                        </button>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          )}
        </section>

        {/* 予約一覧 */}
        {selectedMember && (
          <section>
            <h4>{selectedMember.name} の予約一覧</h4>
            {activeReservations.length === 0 ? (
              <p className={styles.noData}>有効な予約はありません</p>
            ) : (
              <table className={styles.table}>
                <thead>
                  <tr><th>タイトル</th><th>予約日時</th><th>状態</th><th></th></tr>
                </thead>
                <tbody>
                  {activeReservations.map((r) => (
                    <tr key={r.id}>
                      <td>{r.bookTitle}</td>
                      <td>{r.reservedAt.substring(0, 16).replace('T', ' ')}</td>
                      <td><span className={STATUS_CLASS[r.status]}>{STATUS_LABEL[r.status]}</span></td>
                      <td>
                        <button className={styles.cancelBtn} onClick={() => handleCancel(r)}>
                          キャンセル
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </section>
        )}
      </div>
    </div>
  )
}
