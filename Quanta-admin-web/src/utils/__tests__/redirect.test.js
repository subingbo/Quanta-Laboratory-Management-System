import { describe, expect, it } from 'vitest'
import { safeInternalRedirect } from '../redirect'

describe('safeInternalRedirect', () => {
  it('accepts site-local paths', () => {
    expect(safeInternalRedirect('/members?cohort=21')).toBe('/members?cohort=21')
  })

  it('rejects protocol-relative and external paths', () => {
    expect(safeInternalRedirect('//evil.example')).toBe('/admin/dashboard')
    expect(safeInternalRedirect('https://evil.example')).toBe('/admin/dashboard')
  })
})
