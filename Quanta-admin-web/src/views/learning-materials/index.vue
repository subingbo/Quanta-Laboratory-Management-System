<script setup>
import { computed, onMounted, ref } from 'vue'
import { FolderOpened } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PermissionButton from '@/components/PermissionButton.vue'
import { downloadMaterial, getMaterials, removeMaterial, uploadMaterial } from '@/api/materials'
import { saveBlob } from '@/utils/download'
import { usePermission } from '@/composables/usePermission'
import './learning-materials.css'

const rows = ref([])
const loading = ref(false)
const failed = ref(false)
const uploading = ref(false)
const { hasAny, hasRole } = usePermission()
const canWrite = computed(
  () =>
    (hasRole('qt_mgmt') || hasRole('ceo')) &&
    (hasAny('qt:material:add') || hasAny('qt:material:remove')),
)
const visibilityLabels = {
  ALL: '所有人可见',
  MEMBER: '仅塔员可见',
  DEPT: '仅本部门可见',
}

async function loadList() {
  loading.value = true
  failed.value = false
  try { rows.value = (await getMaterials()).rows }
  catch (error) { failed.value = true; ElMessage.error(error.message || '学习资料加载失败') }
  finally { loading.value = false }
}

function validateFile(file) {
  const extension = file.name.split('.').pop()?.toLowerCase()
  if (!['pdf', 'doc', 'docx', 'ppt', 'pptx', 'fig', 'md'].includes(extension)) {
    ElMessage.warning('仅支持 PDF、Word、PPT、Figma 和 Markdown 文件')
    return false
  }
  if (file.size > 50 * 1024 * 1024) {
    ElMessage.warning('单个文件不能超过50MB')
    return false
  }
  return true
}

async function handleUpload({ file }) {
  uploading.value = true
  try { await uploadMaterial(file); ElMessage.success('上传成功'); await loadList() }
  catch (error) { ElMessage.error(error.message || '上传失败') }
  finally { uploading.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除“${row.fileName}”吗？`, '删除学习资料', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
    await removeMaterial(row.materialId)
    ElMessage.success('删除成功')
    await loadList()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message || '删除失败')
  }
}

async function handleDownload(row) {
  try {
    const result = await downloadMaterial(row)
    saveBlob(result.blob, result.fileName)
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error(error.message || '下载失败')
  }
}

onMounted(loadList)
</script>

<template>
  <div class="learning-materials-view">
    <section v-if="canWrite" class="page-card learning-upload-card">
      <header><h2>上传学习资料</h2></header>
      <ElUpload drag action="#" :show-file-list="false" :before-upload="validateFile" :http-request="handleUpload" :disabled="uploading" accept=".pdf,.doc,.docx,.ppt,.pptx,.fig,.md">
        <ElIcon class="learning-upload-card__icon"><FolderOpened /></ElIcon>
        <p>点击或拖拽文件到此处上传</p>
        <span>支持 PDF, Word, PPT, Figma 等格式，单文件不超过 50MB</span>
      </ElUpload>
    </section>

    <section class="page-card learning-list-card">
      <header><h2>资料列表</h2></header>
      <div v-if="failed && !loading" class="learning-list-card__error"><span>资料暂时无法加载。</span><ElButton type="primary" link @click="loadList">重新加载</ElButton></div>
      <ElTable v-else v-loading="loading" class="learning-materials-table" :data="rows" size="small" row-key="materialId" empty-text="暂无学习资料">
        <ElTableColumn prop="fileName" label="文件名" min-width="300"><template #default="{ row }"><ElButton link class="learning-materials-table__download" @click="handleDownload(row)">{{ row.fileName }}</ElButton></template></ElTableColumn>
        <ElTableColumn label="分类" min-width="160"><template #default="{ row }"><span class="learning-materials-table__tag">{{ row.category }}</span></template></ElTableColumn>
        <ElTableColumn prop="fileSize" label="大小" min-width="130" />
        <ElTableColumn prop="uploader" label="上传者" min-width="140" />
        <ElTableColumn prop="uploadedAt" label="上传时间" min-width="230" />
        <ElTableColumn label="可见性" min-width="170"><template #default="{ row }"><span class="learning-materials-table__visibility" :class="{ 'is-restricted': row.visibility !== 'ALL' }">{{ visibilityLabels[row.visibility] || row.visibility }}</span></template></ElTableColumn>
        <ElTableColumn v-if="canWrite" label="操作" min-width="110"><template #default="{ row }"><PermissionButton link type="danger" permissions="qt:material:remove" :roles="['qt_mgmt', 'ceo']" @click="handleDelete(row)">删除</PermissionButton></template></ElTableColumn>
      </ElTable>
    </section>
  </div>
</template>
