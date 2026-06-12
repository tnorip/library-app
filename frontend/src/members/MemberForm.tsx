import { useState } from 'react'
import { createMember, type MemberRequest } from '../api/client'
import styles from './MemberForm.module.css'

interface Props {
  onCreated: () => void
}

const empty: MemberRequest = { memberNumber: '', name: '', email: '', phone: '' }

export default function MemberForm({ onCreated }: Props) {
  const [form, setForm] = useState<MemberRequest>(empty)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      await createMember(form)
      setForm(empty)
      onCreated()
    } catch (err) {
      setError(err instanceof Error ? err.message : '登録に失敗しました')
    }
  }

  const set = (field: keyof MemberRequest) =>
    (e: React.ChangeEvent<HTMLInputElement>) =>
      setForm((f) => ({ ...f, [field]: e.target.value }))

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      <h3>利用者登録</h3>
      {error && <p className={styles.error}>{error}</p>}
      <div className={styles.row}>
        <input required placeholder="利用者番号 (例: M-00001)" value={form.memberNumber} onChange={set('memberNumber')} />
        <input required placeholder="氏名" value={form.name} onChange={set('name')} />
        <input required type="email" placeholder="メールアドレス" value={form.email} onChange={set('email')} />
        <input required placeholder="電話番号" value={form.phone} onChange={set('phone')} />
        <button type="submit">登録</button>
      </div>
    </form>
  )
}
