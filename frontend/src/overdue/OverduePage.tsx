import { useEffect, useState } from 'react'
import { fetchOverdue, sendReminders, type OverdueItem } from '../api/client'
import styles from './OverduePage.module.css'

export default function OverduePage() {
  const [items, setItems]     = useState<OverdueItem[]>([])
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState('')
  const [error, setError]     = useState('')

  useEffect(() => {
    fetchOverdue()
      .then(setItems)
      .catch(() => setError('延滞情報の取得に失敗しました'))
      .finally(() => setLoading(false))
  }, [])

  const handleNotify = async () => {
    if (items.length === 0) return
    if (!confirm(`${items.length} 件に督促通知を送りますか？`)) return
    setMessage('')
    setError('')
    try {
      const result = await sendReminders()
      setMessage(`${result.length} 件に督促通知を送信しました（ログに記録済み）`)
    } catch (err) {
      setError(err instanceof Error ? err.message : '通知送信に失敗しました')
    }
  }

  const urgentCount = items.filter((i) => i.overdueDays >= 7).length

  return (
    <div>
      <h3>延滞管理・督促通知</h3>

      {loading && <p>読み込み中...</p>}
      {error   && <p className={styles.error}>{error}</p>}
      {message && <p className={styles.success}>{message}</p>}

      {!loading && (
        <>
          <div className={styles.summary}>
            <div className={styles.summaryCard}>
              <span className={styles.summaryNum}>{items.length}</span>
              <span className={styles.summaryLabel}>件 延滞中</span>
            </div>
            {urgentCount > 0 && (
              <div className={`${styles.summaryCard} ${styles.urgent}`}>
                <span className={styles.summaryNum}>{urgentCount}</span>
                <span className={styles.summaryLabel}>件 7日超過</span>
              </div>
            )}
            <button
              className={styles.notifyBtn}
              onClick={handleNotify}
              disabled={items.length === 0}
            >
              全員に督促通知を送る
            </button>
          </div>

          {items.length === 0 ? (
            <p className={styles.noData}>現在、延滞中の貸出はありません</p>
          ) : (
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>利用者名</th>
                  <th>メールアドレス</th>
                  <th>書名</th>
                  <th>返却期限</th>
                  <th>延滞日数</th>
                </tr>
              </thead>
              <tbody>
                {items
                  .sort((a, b) => b.overdueDays - a.overdueDays)
                  .map((item) => (
                    <tr key={item.loanId} className={item.overdueDays >= 7 ? styles.urgentRow : ''}>
                      <td>{item.memberName}</td>
                      <td>{item.memberEmail}</td>
                      <td>{item.bookTitle}</td>
                      <td>{item.dueDate}</td>
                      <td className={styles.days}>
                        <span className={item.overdueDays >= 7 ? styles.urgent : styles.normal}>
                          {item.overdueDays} 日
                        </span>
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
