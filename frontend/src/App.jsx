import { useEffect, useState } from 'react'
import { extensionApi } from './api'
import FixedExtensions from './components/FixedExtensions'
import CustomExtensions from './components/CustomExtensions'
import FileUpload from './components/FileUpload'

export default function App() {
  const [fixed, setFixed] = useState([])
  const [custom, setCustom] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  async function fetchExtensions() {
    try {
      const data = await extensionApi.getAll()
      setFixed([...data.fixed].sort((a, b) => b.isBlocked - a.isBlocked))
      setCustom(data.custom)
      setError('')
    } catch (e) {
      setError('확장자 목록을 불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchExtensions() }, [])

  return (
    <main>
      <h1>파일 확장자 차단 관리</h1>
      {error && <p className="error">{error}</p>}
      {loading ? (
        <p>불러오는 중...</p>
      ) : (
        <>
          <FixedExtensions items={fixed} onChange={fetchExtensions} />
          <hr />
          <CustomExtensions items={custom} onChange={fetchExtensions} />
          <hr />
          <FileUpload />
        </>
      )}
    </main>
  )
}
