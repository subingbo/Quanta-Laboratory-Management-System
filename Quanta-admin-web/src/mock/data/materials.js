const initialMaterials = [
  { materialId: 1, fileName: 'React入门教程.pdf', category: '前端开发', fileSize: '2.3MB', uploaderName: '王建国', uploadTime: '2025-03-15 14:00', visibility: 'ALL', downloadUrl: '/qt/materials/1/download' },
  { materialId: 2, fileName: '产品需求文档模板.docx', category: '产品设计', fileSize: '1.1MB', uploaderName: '李明华', uploadTime: '2025-03-20 10:30', visibility: 'ALL', downloadUrl: '/qt/materials/2/download' },
  { materialId: 3, fileName: 'Figma组件库.fig', category: 'UI设计', fileSize: '15.6MB', uploaderName: '张晓雪', uploadTime: '2025-04-01 16:00', visibility: 'ALL', downloadUrl: '/qt/materials/3/download' },
  { materialId: 4, fileName: 'Python数据分析笔记.md', category: '数据分析', fileSize: '0.5MB', uploaderName: '王建国', uploadTime: '2025-04-10 11:00', visibility: 'MEMBER', downloadUrl: '/qt/materials/4/download' },
]

export let mockMaterials = structuredClone(initialMaterials)
export function resetMockMaterials() { mockMaterials = structuredClone(initialMaterials) }
