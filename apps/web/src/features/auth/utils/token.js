function decodePayload(token) {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(atob(base64))
  } catch {
    return null
  }
}

export function isTokenExpired(token) {
  const payload = decodePayload(token)
  if (!payload?.exp) return false
  return payload.exp * 1000 <= Date.now()
}
