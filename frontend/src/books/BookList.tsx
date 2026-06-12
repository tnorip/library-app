import { useEffect, useState } from 'react'
import { fetchBooks, deleteBook, type Book } from '../api/client'
import styles from './BookList.module.css'

interface Props {
  refreshKey: number
}

export default function BookList({ refreshKey }: Props) {
  const [books, setBooks] = useState<Book[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [query, setQuery] = useState('')

  useEffect(() => {
    setLoading(true)
    setError(false)
    fetchBooks(query || undefined)
      .then((result) => setBooks(result))
      .catch(() => setError(true))
      .finally(() => setLoading(false))
  }, [refreshKey, query])

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
              <th>タイトル</th>
              <th>著者</th>
              <th>ISBN</th>
              <th>出版社</th>
              <th>出版年</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {books.map((book) => (
              <tr key={book.id}>
                <td>{book.title}</td>
                <td>{book.author}</td>
                <td>{book.isbn}</td>
                <td>{book.publisher}</td>
                <td>{book.publishedYear}</td>
                <td>
                  <button
                    className={styles.deleteBtn}
                    onClick={() => handleDelete(book)}
                  >
                    削除
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}
