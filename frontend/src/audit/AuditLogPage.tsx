import { useEffect, useState } from 'react'
import { fetchAuditLogs, type AuditLog } from '../api/client'
import styles from './AuditLogPage.module.css'

const ACTION_LABEL: Record<string, string> = {
  BOOK_CREATED:          '図書 登録',
  BOOK_UPDATED:          '図書 更新',
  MEMBER_CREATED:        '利用者 登録',
  MEMBER_UPDATED:        '利用者 更新',
  LOAN_CHECKOUT:         '貸出',
  LOAN_RETURNED:         '返却',
  RESERVATION_CREATED:   '予約',
}

export default function AuditLogPage() {
  const [logs, setLogs]       = useState<AuditLog[]>([])
  const [filter, setFilter]   = useState('')
  const [search, setSearch]   = useState('')
  const [error, setError]     = useState('')

  const load = (user?: string) => {
    setError('')
    fetchAuditLogs(user || undefined)
      .then(setLogs)
      .catch(() => setError('ログの取得に失敗しました'))
  }

  useEffect(() => { load() }, [])

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    load(filter)
  }

  const handleReset = () => {
    setFilter('')
    setSearch('')
    load()
  }

  const displayed = search
    ? logs.filter((l) =>
        l.action.includes(search.toUpperCase()) ||
        l.performedBy.includes(search) ||
        l.targetType.includes(search))
    : logs

  return (
    <div>
      <h3>操作履歴・監査ログ</h3>

      <div className={styles.toolbar}>
        <form onSubmit={handleSearch} className={styles.form}>
          <input
            placeholder="操作者で絞り込み..."
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
          />
          <button type="submit">絞り込み</button>
          <button type="button" onClick={handleReset}>リセット</button>
        </form>
        <input
          className={styles.search}
          placeholder="アクション・種別で検索..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {error && <p className={styles.error}>{error}</p>}

      <p className={styles.count}>{displayed.length} 件</p>

      <table className={styles.table}>
        <thead>
          <tr>
            <th>日時</th>
            <th>操作</th>
            <th>対象種別</th>
            <th>対象ID</th>
            <th>操作者</th>
          </tr>
        </thead>
        <tbody>
          {displayed.map((log) => (
            <tr key={log.id}>
              <td className={styles.timestamp}>
                {log.performedAt.substring(0, 19).replace('T', ' ')}
              </td>
              <td>
                <span className={styles.action}>
                  {ACTION_LABEL[log.action] ?? log.action}
                </span>
              </td>
              <td>{log.targetType}</td>
              <td>{log.targetId ?? '—'}</td>
              <td>{log.performedBy}</td>
            </tr>
          ))}
          {displayed.length === 0 && (
            <tr><td colSpan={5} className={styles.empty}>ログがありません</td></tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
