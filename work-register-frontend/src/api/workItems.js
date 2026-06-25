const API_BASE = '/api/work-items'

async function request(url, options = {}) {
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers
    },
    ...options
  })

  const payload = await response.json().catch(() => null)

  if (!response.ok) {
    throw new Error(payload?.message || `请求失败：${response.status}`)
  }

  if (payload?.code !== 0) {
    throw new Error(payload?.message || '接口返回失败')
  }

  return payload.data
}

export function fetchWorkItems(params = {}) {
  const query = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      query.append(key, value)
    }
  })

  const suffix = query.toString() ? `?${query.toString()}` : ''
  return request(`${API_BASE}${suffix}`)
}

export function createWorkItem(data) {
  return request(API_BASE, {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

export function completeWorkItem(id) {
  return request(`${API_BASE}/${id}/complete`, {
    method: 'PATCH'
  })
}

export function fetchSummary(type) {
  return request(`${API_BASE}/summary?type=${type}`)
}

export function fetchWeeklyContentSummary() {
  return request(`${API_BASE}/weekly-content-summary`)
}
