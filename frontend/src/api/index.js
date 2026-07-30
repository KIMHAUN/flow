const BASE = (import.meta.env.VITE_API_BASE ?? '') + '/api'

async function request(url, options = {}) {
  const res = await fetch(BASE + url, options)
  const json = await res.json()
  if (!json.success) throw new Error(json.message || '요청에 실패했습니다.')
  return json.data
}

export const extensionApi = {
  getAll: () => request('/extensions'),

  updateFixed: (extension, isBlocked) =>
    request(`/extensions/fixed/${extension}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ isBlocked }),
    }),

  addCustom: (extension) =>
    request('/extensions/custom', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ extension }),
    }),

  deleteCustom: (extension) =>
    request(`/extensions/custom/${extension}`, { method: 'DELETE' }),
}

