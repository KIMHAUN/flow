import { useState } from 'react'
import { uploadApi } from '../api'

export default function FileUpload() {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  async function handleChange(e) {
    const file = e.target.files[0]
    if (!file) return

    setLoading(true)
    setError('')
    setSuccess('')
    try {
      await uploadApi.upload(file)
      setSuccess(`${file.name} 업로드 성공`)
    } catch (err) {
      setError(err.message)
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
      {error && <p className="error">{error}</p>}
      {success && <p className="success">{success}</p>}
    </section>
  )
}
