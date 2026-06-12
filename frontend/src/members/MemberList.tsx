import { useEffect, useState } from 'react'
import { fetchMembers, deactivateMember, type Member } from '../api/client'
import styles from './MemberList.module.css'

interface Props {
  refreshKey: number
  onSelect: (member: Member) => void
}

export default function MemberList({ refreshKey, onSelect }: Props) {
  const [members, setMembers] = useState<Member[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    fetchMembers()
      .then(setMembers)
      .catch(() => setError('利用者の取得に失敗しました'))
  }, [refreshKey])

  const handleDeactivate = async (m: Member) => {
    if (!confirm(`「${m.name}」を退会処理しますか？`)) return
    try {
      await deactivateMember(m.id)
      setMembers((prev) => prev.filter((x) => x.id !== m.id))
    } catch (e) {
      alert(e instanceof Error ? e.message : '退会処理に失敗しました')
    }
  }

  if (error) return <p className={styles.error}>{error}</p>

  return (
    <table className={styles.table}>
      <thead>
        <tr>
          <th>利用者番号</th><th>氏名</th><th>メール</th><th>電話</th><th>操作</th>
        </tr>
      </thead>
      <tbody>
        {members.filter((m) => m.active).map((m) => (
          <tr key={m.id}>
            <td>{m.memberNumber}</td>
            <td>
              <button className={styles.link} onClick={() => onSelect(m)}>{m.name}</button>
            </td>
            <td>{m.email}</td>
            <td>{m.phone}</td>
            <td>
              <button className={styles.danger} onClick={() => handleDeactivate(m)}>退会</button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
