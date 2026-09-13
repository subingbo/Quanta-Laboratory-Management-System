import { readFileSync } from 'node:fs'
import { describe, expect, it } from 'vitest'

const source = readFileSync(new URL('./process.vue', import.meta.url), 'utf8')
const emptyStateRule = source.match(/\.empty-state\{([^}]*)\}/)?.[1] ?? ''

describe('Join Us empty state layout', () => {
  it('centers the prompt in the remaining content area without a fixed top offset', () => {
    expect(emptyStateRule).toContain('align-items:center')
    expect(emptyStateRule).toContain('justify-content:center')
    expect(emptyStateRule).not.toContain('padding-top:')
  })
})
