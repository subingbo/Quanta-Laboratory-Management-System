import request from '../utils/request'

/**
 * 塔服订购 —— 真实后端对接层。
 * 页面契约与 utils/memberMock 的 getShirtProduct / saveMockShirtOrder 对齐,
 * 数据源切换为 /qt/item + /qt/order。
 */

interface QtClothingItemRow {
  itemId: number
  itemName: string
  effectImageUrl?: string
  colorOptionsJson?: string
  sizeOptionsJson?: string
  status?: string // "0"上架 "1"下架
}

export interface ShirtProduct {
  itemId: number
  itemName: string
  images: string[]
  colors: string[]
  sizes: string[]
  price: number
}

const parseJsonArray = (json: string | undefined, fallback: string[]): string[] => {
  if (!json) return fallback
  try {
    const list = JSON.parse(json)
    return Array.isArray(list) ? list.map(String) : fallback
  } catch {
    return fallback
  }
}

let cachedProduct: ShirtProduct | null = null

export const getShirtProduct = async (): Promise<ShirtProduct> => {
  if (cachedProduct) return cachedProduct
  const res = await request<{ rows?: QtClothingItemRow[] }>({ url: '/qt/item/list?pageNum=1&pageSize=10', method: 'GET' })
  const item = (res?.rows || []).find((row) => String(row.status) !== '1') || (res?.rows || [])[0]
  cachedProduct = {
    itemId: item?.itemId ?? 0,
    itemName: item?.itemName ?? '',
    images: item?.effectImageUrl ? [item.effectImageUrl] : [],
    colors: parseJsonArray(item?.colorOptionsJson, []),
    sizes: parseJsonArray(item?.sizeOptionsJson, []),
    price: 0, // 后端暂无单价字段,价格到货后按实物通知
  }
  return cachedProduct
}

export const saveShirtOrder = async (selection: { color: string; size: string }): Promise<void> => {
  const product = await getShirtProduct()
  const orderNo = `QT${Date.now()}${Math.random().toString(36).slice(2, 8).toUpperCase()}`
  await request({
    url: '/qt/order',
    method: 'POST',
    data: {
      itemId: product.itemId,
      orderNo,
      selectedColor: selection.color,
      selectedSize: selection.size,
      quantity: 1,
      status: 'DRAFT', // 草稿待后续补付款截图再提交,避开发票触发器
    },
  })
}