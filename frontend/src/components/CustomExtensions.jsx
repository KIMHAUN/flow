import { useState } from 'react'
import { extensionApi } from '../api'

const MAX = 200

export default function CustomExtensions({ items, onChange }) {
  const [input, setInput] = useState('')
  const [error, setError] = useState('')

  async function handleAdd() {
    const value = input.trim()
    if (!value) return

    try {
      await extensionApi.addCustom(value)
      setInput('')
      setError('')
      onChange()
    } catch (e) {
      setError(e.message)
    }
  }

  async function handleDelete(extension) {
    try {
      await extensionApi.deleteCustom(extension)
      onChange()
    } catch (e) {
      alert(e.message)
    }
  }

  function handleKeyDown(e) {
    if (e.key === 'Enter') handleAdd()
  }

  return (
    <section>
      <h2>커스텀 확장자</h2>
      <div className="custom-input-row">
        <input
          type="text"
          value={input}
          maxLength={20}
          placeholder="확장자 입력"
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={items.length >= MAX}
        />
        <button onClick={handleAdd} disabled={items.length >= MAX}>
          + 추가
        </button>
        <span className="count">{items.length}/{MAX}</span>
      </div>
      {error && <p className="error">{error}</p>}
      <div className="custom-list">
        {items.map((ext) => (
          <span key={ext} className="chip">
            {ext}
            <button className="delete-btn" onClick={() => handleDelete(ext)}>✕</button>
          </span>
        ))}
      </div>
    </section>
  )
}
