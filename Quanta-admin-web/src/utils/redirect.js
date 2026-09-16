export function safeInternalRedirect(value, fallback = '/admin/dashboard') {
  if (typeof value !== 'string') return fallback
  if (!value.startsWith('/') || value.startsWith('//')) return fallback
  if (value.includes('\\')) return fallback
  if ([...value].some((character) => {
    const code = character.charCodeAt(0)
    return code < 32 || code === 127
  })) return fallback
  return value
}
