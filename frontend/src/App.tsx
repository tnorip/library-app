import { useState } from 'react'
import BookForm from './books/BookForm'
import BookList from './books/BookList'
import MemberForm from './members/MemberForm'
import MemberList from './members/MemberList'
import LoanPage from './loans/LoanPage'
import LoanRulePage from './loanrules/LoanRulePage'
import type { Member } from './api/client'

type Tab = 'books' | 'members' | 'loans' | 'rules'

const TAB_LABELS: Record<Tab, string> = {
  books:   '図書管理',
  members: '利用者管理',
  loans:   '貸出・返却',
  rules:   '貸出ルール',
}

export default function App() {
  const [tab, setTab] = useState<Tab>('books')
  const [bookRefresh, setBookRefresh] = useState(0)
  const [memberRefresh, setMemberRefresh] = useState(0)
  const [, setSelectedMember] = useState<Member | null>(null)

  return (
    <div style={{ maxWidth: '960px', margin: '0 auto', padding: '1rem' }}>
      <h1 style={{ marginBottom: '0.5rem' }}>みどり市立図書館</h1>

      {/* タブ */}
      <nav style={{ display: 'flex', gap: '0.25rem', marginBottom: '1.5rem', borderBottom: '2px solid #ddd' }}>
        {(Object.keys(TAB_LABELS) as Tab[]).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            style={{
              padding: '0.5rem 1.2rem',
              border: 'none',
              borderBottom: tab === t ? '2px solid #2a7' : '2px solid transparent',
              background: 'none',
              fontWeight: tab === t ? 'bold' : 'normal',
              color: tab === t ? '#196' : '#555',
              cursor: 'pointer',
              marginBottom: '-2px',
            }}
          >
            {TAB_LABELS[t]}
          </button>
        ))}
      </nav>

      {/* コンテンツ */}
      {tab === 'books' && (
        <>
          <BookForm onCreated={() => setBookRefresh((k) => k + 1)} />
          <BookList refreshKey={bookRefresh} />
        </>
      )}

      {tab === 'members' && (
        <>
          <MemberForm onCreated={() => setMemberRefresh((k) => k + 1)} />
          <MemberList
            refreshKey={memberRefresh}
            onSelect={(m) => {
              setSelectedMember(m)
              setTab('loans')
            }}
          />
        </>
      )}

      {tab === 'loans' && <LoanPage />}

      {tab === 'rules' && <LoanRulePage />}
    </div>
  )
}
