import { useState } from 'react'
import { createBook, type BookRequest } from '../api/client'
import styles from './BookForm.module.css'

interface Props {
  onCreated: () => void
}

const EMPTY: BookRequest = {
  isbn: '',
  title: '',
  author: '',
  publisher: '',
  publishedYear: new Date().getFullYear(),
  callNumber: '',
}

export default function BookForm({ onCreated }: Props) {
  const [form, setForm] = useState<BookRequest>(EMPTY)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const { name, value } = e.target
    setForm((prev) => ({
      ...prev,
      [name]: name === 'publishedYear' ? Number(value) : value,
    }))
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)

    if (!form.title.trim() || !form.author.trim()) {
      setError('タイトルと著者は必須です。')
      return
    }

    setSubmitting(true)
    const result = await createBook(form)
    setSubmitting(false)

    if (result === null) {
      setError('登録に失敗しました。入力内容を確認してください。')
      return
    }

    setForm(EMPTY)
    onCreated()
  }

  return (
    <form onSubmit={handleSubmit} className={styles.form}>
      <h2 className={styles.heading}>図書を登録する</h2>

      {error && <p className={styles.error}>{error}</p>}

      <div className={styles.row}>
        <label>タイトル *
          <input name="title" value={form.title} onChange={handleChange} required />
        </label>
        <label>著者 *
          <input name="author" value={form.author} onChange={handleChange} required />
        </label>
      </div>
      <div className={styles.row}>
        <label>ISBN
          <input name="isbn" value={form.isbn} onChange={handleChange} />
        </label>
        <label>出版社
          <input name="publisher" value={form.publisher} onChange={handleChange} />
        </label>
      </div>
      <div className={styles.row}>
        <label>出版年
          <input name="publishedYear" type="number" value={form.publishedYear} onChange={handleChange} />
        </label>
        <label>分類番号
          <input name="callNumber" value={form.callNumber} onChange={handleChange} />
        </label>
      </div>

      <button type="submit" disabled={submitting} className={styles.button}>
        {submitting ? '登録中...' : '登録'}
      </button>
    </form>
  )
}
