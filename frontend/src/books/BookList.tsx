import { useEffect, useState } from 'react'
import { fetchBooks, deleteBook, type Book, type CopyStatus } from '../api/client'
import styles from './BookList.module.css'

interface Props {
  refreshKey: number
}

const STATUS_LABEL: Record<CopyStatus, string> = {
  AVAILABLE:   '在庫',
  CHECKED_OUT: '貸出中',
  REPAIR:      '修繕中',
  DISCARDED:   '廃棄',
}

const STATUS_CLASS: Record<CopyStatus, string> = {
  AVAILABLE:   styles.available,
  CHECKED_OUT: styles.checkedOut,
  REPAIR:      styles.repair,
  DISCARDED:   styles.discarded,
}

export default function BookList({ refreshKey }: Props) {
  const [books, setBooks] = useState<Book[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [query, setQuery] = useState('')
  const [expanded, setExpanded] = useState<Set<number>>(new Set())

  useEffect(() => {
    setLoading(true)
    setError(false)
    fetchBooks(query || undefined)
      .then((result) => setBooks(result))
      .catch(() => setError(true))
      .finally(() => setLoading(false))
  }, [refreshKey, query])

  const toggleExpand = (id: number) =>
    setExpanded((prev) => {
      const next = new Set(prev)
      next.has(id) ? next.delete(id) : next.add(id)
      return next
    })

  async function handleDelete(book: Book) {
    if (!window.confirm(`「${book.title}」を削除しますか？`)) return
    try {
      await deleteBook(book.id)
      setBooks((prev) => prev.filter((b) => b.id !== book.id))
    } catch (err) {
      alert(err instanceof Error ? err.message : '削除に失敗しました。')
    }
  }

  return (
    <section>
      <div className={styles.toolbar}>
        <h2 className={styles.heading}>図書一覧</h2>
        <input
          className={styles.search}
          type="search"
          placeholder="タイトル・著者で検索..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
      </div>

      {loading && <p className={styles.status}>読み込み中...</p>}
      {error   && <p className={styles.error}>図書一覧の取得に失敗しました。</p>}
      {!loading && !error && books.length === 0 && (
        <p className={styles.status}>登録されている図書はありません。</p>
      )}

      {!loading && !error && books.length > 0 && (
        <table className={styles.table}>
          <thead>
            <tr>
              <th></th>
              <th>タイトル</th>
              <th>著者</th>
              <th>請求記号</th>
              <th>ISBN</th>
              <th>出版年</th>
              <th>在庫状況</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {books.map((book) => {
              const isOpen = expanded.has(book.id)
              const availableCount = book.copies.filter((c) => c.status === 'AVAILABLE').length
              return (
                <>
                  <tr key={book.id}>
                    <td>
                      {book.copies.length > 0 && (
                        <button className={styles.expandBtn} onClick={() => toggleExpand(book.id)}>
                          {isOpen ? '▲' : '▼'}
                        </button>
                      )}
                    </td>
                    <td>{book.title}</td>
                    <td>{book.author}</td>
                    <td>{book.callNumber}</td>
                    <td>{book.isbn}</td>
                    <td>{book.publishedYear}</td>
                    <td>
                      <span className={availableCount > 0 ? styles.available : styles.checkedOut}>
                        {availableCount > 0 ? `在庫 ${availableCount}冊` : '貸出中'}
                      </span>
                    </td>
                    <td>
                      <button className={styles.deleteBtn} onClick={() => handleDelete(book)}>削除</button>
                    </td>
                  </tr>
                  {isOpen && book.copies.map((copy) => (
                    <tr key={`copy-${copy.id}`} className={styles.copyRow}>
                      <td></td>
                      <td colSpan={2} className={styles.copyCode}>└ {copy.copyCode}</td>
                      <td colSpan={2}>
                        <span className={STATUS_CLASS[copy.status]}>
                          {STATUS_LABEL[copy.status]}
                        </span>
                        {copy.status === 'CHECKED_OUT' && copy.dueDate && (
                          <span className={styles.dueDate}> 返却期限: {copy.dueDate}</span>
                        )}
                      </td>
                      <td colSpan={3}></td>
                    </tr>
                  ))}
                </>
              )
            })}
          </tbody>
        </table>
      )}
    </section>
  )
}
