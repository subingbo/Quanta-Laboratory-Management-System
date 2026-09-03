import { findAccountByToken } from '../data/accounts'
import { mockMaterials } from '../data/materials'

const allowedExtensions = ['pdf', 'doc', 'docx', 'ppt', 'pptx', 'fig', 'md']
const categoryByExtension = { pdf: '前端开发', doc: '产品设计', docx: '产品设计', ppt: '产品设计', pptx: '产品设计', fig: 'UI设计', md: '数据分析' }
const has = (account, permission) => account?.permissions.includes('*:*:*') || account?.permissions.includes(permission)
const account = (config) => findAccountByToken(config.headers?.Authorization || '')

export const materialHandlers = [
  {
    method: 'get', path: '/qt/materials', handle(config) {
      const current = account(config)
      if (!current) return { code: 401, msg: '登录状态已失效' }
      if (!has(current, 'qt:material:list')) return { code: 403, msg: '没有查看学习资料的权限' }
      return { code: 200, rows: mockMaterials, total: mockMaterials.length }
    },
  },
  {
    method: 'post', path: '/qt/materials', handle(config) {
      const current = account(config)
      if (!current) return { code: 401, msg: '登录状态已失效' }
      if (!has(current, 'qt:material:add')) return { code: 403, msg: '没有上传学习资料的权限' }
      const file = config.data instanceof FormData ? config.data.get('file') : null
      if (!file) return { code: 400, msg: '请选择文件' }
      if (file.size > 50 * 1024 * 1024) return { code: 400, msg: '单个文件不能超过50MB' }
      const extension = file.name.split('.').pop()?.toLowerCase()
      if (!allowedExtensions.includes(extension)) return { code: 400, msg: '暂不支持该文件格式' }
      const materialId = Date.now()
      mockMaterials.unshift({
        materialId, fileName: file.name, category: categoryByExtension[extension] || '其他',
        fileSize: `${Math.max(file.size / 1024 / 1024, 0.1).toFixed(1)}MB`, uploaderName: current.user.nickName,
        uploadTime: new Date().toLocaleString('zh-CN', { hour12: false }).replaceAll('/', '-'),
        visibility: config.data.get('visibility') || 'MEMBER',
        downloadUrl: `/qt/materials/${materialId}/download`,
      })
      return { code: 200, msg: '上传成功' }
    },
  },
  {
    method: 'get', match: (path) => /^\/qt\/materials\/\d+\/download$/.test(path), handle(config) {
      const current = account(config)
      if (!current) return { code: 401, msg: '登录状态已失效' }
      const id = Number(config.path.split('/').at(-2))
      const material = mockMaterials.find((item) => item.materialId === id)
      if (!material) return { code: 404, msg: '学习资料不存在' }
      return {
        code: 200,
        data: { fileName: material.fileName, content: `Mock material: ${material.fileName}` },
      }
    },
  },
  {
    method: 'delete', match: (path) => /^\/qt\/materials\/\d+$/.test(path), handle(config) {
      const current = account(config)
      if (!current) return { code: 401, msg: '登录状态已失效' }
      if (!has(current, 'qt:material:remove')) return { code: 403, msg: '没有删除学习资料的权限' }
      const id = Number(config.path.split('/').pop())
      const index = mockMaterials.findIndex((item) => item.materialId === id)
      if (index >= 0) mockMaterials.splice(index, 1)
      return { code: 200, msg: '删除成功' }
    },
  },
]
