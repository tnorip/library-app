import { useEffect, useState } from 'react'
import { fetchLoanRule, updateLoanRule, type LoanRule } from '../api/client'
import styles from './LoanRulePage.module.css'

export default function LoanRulePage() {
  const [rule, setRule] = useState<LoanRule | null>(null)
  const [maxLoanCount, setMaxLoanCount] = useState(10)
  const [loanPeriodDays, setLoanPeriodDays] = useState(14)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    fetchLoanRule()
      .then((r) => {
        setRule(r)
        setMaxLoanCount(r.maxLoanCount)
        setLoanPeriodDays(r.loanPeriodDays)
      })
      .catch(() => setError('貸出ルールの取得に失敗しました'))
  }, [])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setMessage('')
    setError('')
    try {
      const updated = await updateLoanRule({ maxLoanCount, loanPeriodDays })
      setRule(updated)
      setMessage('貸出ルールを更新しました')
    } catch (err) {
      setError(err instanceof Error ? err.message : '更新に失敗しました')
    }
  }

  return (
    <div>
      <h3>貸出ルール設定</h3>
      {error && <p className={styles.error}>{error}</p>}
      {rule && (
        <form className={styles.form} onSubmit={handleSubmit}>
          <label>
            貸出上限冊数
            <input
              type="number" min={1} max={50} required
              value={maxLoanCount}
              onChange={(e) => setMaxLoanCount(Number(e.target.value))}
            />
            冊
          </label>
          <label>
            貸出期間
            <input
              type="number" min={1} max={90} required
              value={loanPeriodDays}
              onChange={(e) => setLoanPeriodDays(Number(e.target.value))}
            />
            日
          </label>
          <button type="submit">保存</button>
          {message && <span className={styles.success}>{message}</span>}
        </form>
      )}
    </div>
  )
}
