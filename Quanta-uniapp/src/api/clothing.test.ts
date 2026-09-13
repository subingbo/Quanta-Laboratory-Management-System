import { describe, expect, it } from 'vitest'
import { mapClothingItem, parseOptionArray } from './clothing'

describe('clothing backend mapping', () => {
  it('parses option arrays safely', () => {
    expect(parseOptionArray('["黑色","白色"]')).toEqual(['黑色', '白色'])
    expect(parseOptionArray('not-json')).toEqual([])
  })

  it('maps an active clothing item', () => {
    expect(mapClothingItem({ itemId: 2, effectImageUrl: '/profile/a.png', colorOptionsJson: '["黑色"]', sizeOptionsJson: '["L"]' })).toMatchObject({ itemId: 2, colors: ['黑色'], sizes: ['L'], price: 45 })
  })
})
