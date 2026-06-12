import { useState } from 'react'
import {
  fetchMembers, fetchLoansByMember, checkout, returnBook,
  type Member, type Loan,
} from '../api/client'
import styles from './LoanPage.module.css'

export default function LoanPage() {
  const [members, setMembers] = useState<Member[]>([])
  const [selectedMember, setSelectedMember] = useState<Member | null>(null)
  const [loans, setLoans] = useState<Loan[]>([])
  const [copyId, setCopyId] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const loadMembers = async () => {
    try {
      const data = await fetchMembers()
      setMembers(data.filter((m) => m.active))
    } catch {
      setError('利用者の取得に失敗しました')
    }
  }

  const selectMember = async (m: Member) => {
    setSelectedMember(m)
    setMessage('')
    setError('')
    try {
      setLoans(await fetchLoansByMember(m.id))
    } catch {
      setLoans([])
    }
  }

  const handleCheckout = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedMember) return
    setMessage('')
    setError('')
    try {
      const loan = await checkout({ memberId: selectedMember.id, bookCopyId: Number(copyId) })
      setLoans((prev) => [loan, ...prev])
      setCopyId('')
      setMessage(`「${loan.bookTitle}」を貸出しました（返却期限: ${loan.dueDate}）`)
    } catch (err) {
      setError(err instanceof Error ? err.message : '貸出処理に失敗しました')
    }
  }

  const handleReturn = async (loan: Loan) => {
    setMessage('')
    setError('')
    try {
      const updated = await returnBook(loan.id)
      setLoans((prev) => prev.map((l) => (l.id === updated.id ? updated : l)))
      setMessage(`「${updated.bookTitle}」を返却しました`)
    } catch (err) {
      setError(err instanceof Error ? err.message : '返却処理に失敗しました')
    }
  }

  const activeLoans = loans.filter((l) => !l.returnedDate)

  return (
    <div>
      <h3>貸出・返却</h3>

      {/* 利用者選択 */}
      <div className={styles.memberSelect}>
        <select
          value={selectedMember?.id ?? ''}
          onChange={(e) => {
            const m = members.find((x) => x.id === Number(e.target.value))
            if (m) selectMember(m)
          }}
          onClick={loadMembers}
        >
          <option value="">-- 利用者を選択 --</option>
          {members.map((m) => (
            <option key={m.id} value={m.id}>{m.memberNumber} {m.name}</option>
          ))}
        </select>
      </div>

      {message && <p className={styles.success}>{message}</p>}
      {error   && <p className={styles.error}>{error}</p>}

      {selectedMember && (
        <>
          {/* 貸出フォーム */}
          <form className={styles.checkoutForm} onSubmit={handleCheckout}>
            <label>冊コードで貸出</label>
            <input
              required
              type="number"
              placeholder="冊ID (bookCopyId)"
              value={copyId}
              onChange={(e) => setCopyId(e.target.value)}
            />
            <button type="submit">貸出</button>
          </form>

          {/* 貸出中一覧 */}
          <h4>{selectedMember.name} の貸出中 ({activeLoans.length}件)</h4>
          {activeLoans.length === 0 ? (
            <p>貸出中の本はありません</p>
          ) : (
            <table className={styles.table}>
              <thead>
                <tr><th>タイトル</th><th>冊コード</th><th>貸出日</th><th>返却期限</th><th>操作</th></tr>
              </thead>
              <tbody>
                {activeLoans.map((l) => (
                  <tr key={l.id}>
                    <td>{l.bookTitle}</td>
                    <td>{l.copyCode}</td>
                    <td>{l.loanDate}</td>
                    <td className={new Date(l.dueDate) < new Date() ? styles.overdue : ''}>{l.dueDate}</td>
                    <td>
                      <button className={styles.returnBtn} onClick={() => handleReturn(l)}>返却</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </div>
  )
}
