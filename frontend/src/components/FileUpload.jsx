import { useState } from 'react'
import { uploadApi } from '../api'

export default function FileUpload() {
  const [status, setStatus] = useState(null)   // { ok: bool, message: string }
  const [loading, setLoading] = useState(false)

  async function handleChange(e) {
    const file = e.target.files[0]
    if (!file) return

    setLoading(true)
    setStatus(null)

    try {
      const result = await uploadApi.upload(file)
      setStatus({ ok: true, message: `업로드 성공: ${result.originalName} (${(result.size / 1024).toFixed(1)} KB)` })
    } catch (err) {
      alert(err.message)
    } finally {
      setLoading(false)
      e.target.value = ''
    }
  }

  return (
    <section>
      <h2>파일 업로드</h2>
      <label className="upload-label">
        {loading ? '업로드 중...' : '파일 선택'}
        <input type="file" onChange={handleChange} disabled={loading} hidden />
      </label>
      {status && (
        <p className={status.ok ? 'success' : 'error'}>{status.message}</p>
      )}
    </section>
  )
}
