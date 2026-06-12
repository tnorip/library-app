import { useState } from 'react'
import BookForm from './books/BookForm'
import BookList from './books/BookList'

function App() {
  const [refreshKey, setRefreshKey] = useState(0)

  return (
    <div style={{ maxWidth: '900px', margin: '0 auto', padding: '1rem' }}>
      <h1>みどり市立図書館</h1>
      <BookForm onCreated={() => setRefreshKey((k) => k + 1)} />
      <BookList refreshKey={refreshKey} />
    </div>
  )
}

export default App
