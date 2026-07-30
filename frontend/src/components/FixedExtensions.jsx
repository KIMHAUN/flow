import { extensionApi } from '../api'

export default function FixedExtensions({ items, onChange }) {
  async function handleToggle(extension, current) {
    try {
      await extensionApi.updateFixed(extension, !current)
      onChange()
    } catch (e) {
      alert(e.message)
    }
  }

  return (
    <section>
      <h2>고정 확장자</h2>
      <div className="fixed-list">
        {items.map(({ extension, isBlocked }) => (
          <label key={extension} className="chip">
            <input
              type="checkbox"
              checked={isBlocked}
              onChange={() => handleToggle(extension, isBlocked)}
            />
            {extension}
          </label>
        ))}
      </div>
    </section>
  )
}
