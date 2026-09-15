export function safeInternalRedirect(value, fallback = '/admin/dashboard') {
  return typeof value === 'string' && value.startsWith('/') && !value.startsWith('//')
    ? value
    : fallback
}
