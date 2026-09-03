import { request } from '@/utils/request'
import { requestBlob } from '@/utils/download'

export function mapMaterial(item = {}) {
  return {
    ...item,
    uploader: item.uploaderName || item.uploader || '-',
    uploadedAt: item.uploadTime || item.createTime || item.uploadedAt || '-',
    downloadUrl: item.downloadUrl || `/qt/materials/${item.materialId}/download`,
  }
}

export async function getMaterials() {
  const response = await request({ url: '/qt/materials', method: 'get' })
  return {
    rows: (response.rows || []).map(mapMaterial),
    total: Number(response.total) || 0,
  }
}

export function uploadMaterial(file, { category = '', visibility = 'MEMBER' } = {}) {
  const data = new FormData()
  data.append('file', file)
  data.append('category', category)
  data.append('visibility', visibility)
  return request({ url: '/qt/materials', method: 'post', data })
}

export function removeMaterial(materialId) {
  return request({ url: `/qt/materials/${materialId}`, method: 'delete' })
}

export function downloadMaterial(material) {
  return requestBlob(
    { url: material.downloadUrl, method: 'get' },
    material.fileName || '学习资料',
  )
}
