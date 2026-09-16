<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { downloadPortalMaterial, getPortalMaterials } from '@/api/portal/content'
import { saveBlob } from '@/utils/download'

const materials = ref([])
const total = ref(0)
const pageNum = ref(1)
const keyword = ref('')
const loading = ref(true)
const downloadingId = ref(null)
const errorMessage = ref('')

async function load() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await getPortalMaterials({
      pageNum: pageNum.value,
      pageSize: 20,
      ...(keyword.value.trim() ? { fileName: keyword.value.trim() } : {}),
    })
    materials.value = result.rows
    total.value = result.total
  } catch (error) {
    errorMessage.value = error.message || '学习资料加载失败'
  } finally {
    loading.value = false
  }
}

async function download(material) {
  downloadingId.value = material.materialId
  try {
    const result = await downloadPortalMaterial(material)
    saveBlob(result.blob, result.fileName)
    ElMessage.success('资料下载已开始')
  } catch (error) {
    ElMessage.error(error.message || '资料下载失败')
  } finally {
    downloadingId.value = null
  }
}

function fileSize(value) {
  const bytes = Number(value || 0)
  if (!bytes) return '未知大小'
  if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

onMounted(load)
</script>

<template>
  <section class="member-page member-materials">
    <header class="member-page__heading">
      <div><p>LEARNING CENTER</p><h1>学习资料</h1><span>浏览并下载 Quanta 内部共享资料。</span></div>
      <form class="member-materials__search" @submit.prevent="load"><input v-model="keyword" type="search" placeholder="搜索资料名称" aria-label="搜索资料" /><button class="member-primary-button">搜索</button></form>
    </header>
    <p v-if="errorMessage" class="member-feedback is-error" role="alert">{{ errorMessage }}</p>
    <div v-if="loading" class="member-loading portal-card">正在加载学习资料…</div>
    <div v-else-if="materials.length" class="member-materials__list portal-card">
      <article v-for="material in materials" :key="material.materialId">
        <div class="member-materials__icon">DOC</div>
        <div><h2>{{ material.fileName }}</h2><p>{{ material.category }} · {{ fileSize(material.fileSize) }} · {{ material.uploader }}</p></div>
        <time>{{ material.uploadedAt || '时间待确认' }}</time>
        <button class="member-secondary-button" :disabled="downloadingId === material.materialId" @click="download(material)">{{ downloadingId === material.materialId ? '下载中…' : '下载' }}</button>
      </article>
      <footer>共 {{ total }} 份资料</footer>
    </div>
    <div v-else class="member-empty portal-card">暂时没有可下载的学习资料</div>
  </section>
</template>

<style src="../member.css"></style>
<style scoped>
.member-materials__search { display: flex; width: min(430px, 100%); gap: 10px; }
.member-materials__search input { min-width: 0; padding: 11px 14px; border: 1px solid var(--portal-line); border-radius: 12px; background: #fff; font: inherit; flex: 1; }
.member-materials__list { padding-inline: 22px; }
.member-materials__list article { display: grid; padding: 19px 2px; border-bottom: 1px solid var(--portal-line); grid-template-columns: auto minmax(0, 1fr) auto auto; align-items: center; gap: 16px; }
.member-materials__icon { display: grid; width: 48px; height: 48px; border-radius: 14px; background: var(--portal-orange-soft); color: var(--portal-orange-strong); font-size: 10px; font-weight: 900; place-items: center; }
.member-materials__list h2, .member-materials__list p { margin: 0; }
.member-materials__list h2 { overflow: hidden; font-size: 15px; text-overflow: ellipsis; white-space: nowrap; }
.member-materials__list p, .member-materials__list time { margin-top: 5px; color: var(--portal-muted); font-size: 11px; }
.member-materials__list footer { padding: 17px 0; color: var(--portal-muted); font-size: 12px; text-align: right; }
@media (max-width: 700px) { .member-materials__list article { grid-template-columns: auto 1fr auto; } .member-materials__list time { display: none; } }
</style>
