<script setup>
import { onMounted, reactive, ref } from 'vue'
import {
  getMyApplication,
  getMyInterviewProcess,
  submitApplication,
} from '@/api/portal/recruitment'
import {
  clearRecruitmentDraft,
  clearRecruitmentPhotoDraft,
  clearRecruitmentResumeDraft,
  loadRecruitmentDraft,
  loadRecruitmentPhotoDraft,
  loadRecruitmentResumeDraft,
  saveRecruitmentDraft,
  saveRecruitmentPhotoDraft,
  saveRecruitmentResumeDraft,
} from '@/utils/recruitment-draft'
import ApplicationForm, { emptyApplication } from './components/ApplicationForm.vue'
import InterviewTimeline from './components/InterviewTimeline.vue'
import PortalNoticeDialog from '@/components/PortalNoticeDialog.vue'

const activeTab = ref('application')
const loading = ref(true)
const submitting = ref(false)
const application = ref(emptyApplication())
const processes = ref([])
const notice = ref('')
const errorMessage = ref('')
const noticeDialog = reactive({ visible: false, title: '', message: '', type: 'success' })

function showResult(title, message, type = 'success') {
  Object.assign(noticeDialog, { visible: true, title, message, type })
}

function showValidationErrors(fields) {
  showResult('请完善报名信息', `请先填写：${fields.join('、')}`, 'error')
}

function friendlySubmissionError(error) {
  const message = String(error?.message || '')
  if (message.includes('两个志愿不能相同')) return message
  if (/网络|timeout|超时|Network/i.test(message)) return '网络连接异常，请稍后重试'
  const backendMessage = String(error?.payload?.msg || error?.payload?.message || '').trim()
  if (backendMessage) return backendMessage
  return '报名提交失败，请检查填写内容后重试'
}

async function loadRecruitment() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [serverApplication, interviewProcesses, draftPhoto, draftResume] = await Promise.all([
      getMyApplication(),
      getMyInterviewProcess(),
      loadRecruitmentPhotoDraft().catch(() => null),
      loadRecruitmentResumeDraft().catch(() => null),
    ])
    application.value = {
      ...emptyApplication(),
      ...(serverApplication || {}),
      ...(loadRecruitmentDraft() || {}),
      ...(draftPhoto ? { photoFile: draftPhoto, photoUrl: '' } : {}),
      ...(draftResume ? { resumeFile: draftResume, resumeFileName: draftResume.name } : {}),
    }
    processes.value = interviewProcesses
  } catch {
    const [draftPhoto, draftResume] = await Promise.all([
      loadRecruitmentPhotoDraft().catch(() => null),
      loadRecruitmentResumeDraft().catch(() => null),
    ])
    application.value = {
      ...emptyApplication(),
      ...(loadRecruitmentDraft() || {}),
      ...(draftPhoto ? { photoFile: draftPhoto, photoUrl: '' } : {}),
      ...(draftResume ? { resumeFile: draftResume, resumeFileName: draftResume.name } : {}),
    }
    errorMessage.value = '招新信息加载失败，请刷新页面重试'
  } finally {
    loading.value = false
  }
}

async function saveDraft(form) {
  try {
    saveRecruitmentDraft(form)
    if (form.photoFile) await saveRecruitmentPhotoDraft(form.photoFile)
    else await clearRecruitmentPhotoDraft()
    if (form.resumeFile) await saveRecruitmentResumeDraft(form.resumeFile)
    else await clearRecruitmentResumeDraft()
    application.value = { ...form }
    notice.value = ''
    errorMessage.value = ''
    await showResult('草稿已保存', '报名信息、证件照和 PDF 简历已保存在当前浏览器。')
  } catch {
    await showResult('保存失败', '草稿保存失败，请检查浏览器存储权限后重试。', 'error')
  }
}

async function submit(form) {
  if (submitting.value) return
  submitting.value = true
  notice.value = ''
  errorMessage.value = ''
  try {
    await submitApplication(form)
    clearRecruitmentDraft()
    await Promise.all([
      clearRecruitmentPhotoDraft().catch(() => {}),
      clearRecruitmentResumeDraft().catch(() => {}),
    ])
    notice.value = ''
    application.value = { ...form }
    try {
      processes.value = await getMyInterviewProcess()
    } catch {
      errorMessage.value = '报名已提交，但进度刷新失败，请稍后在“查看进度”中重试。'
    }
    activeTab.value = 'process'
    await showResult('提交成功', '报名已成功提交，可在“查看进度”中查看后续安排。')
  } catch (error) {
    saveRecruitmentDraft(form)
    if (form.photoFile) await saveRecruitmentPhotoDraft(form.photoFile).catch(() => {})
    if (form.resumeFile) await saveRecruitmentResumeDraft(form.resumeFile).catch(() => {})
    application.value = { ...form }
    errorMessage.value = ''
    await showResult('提交失败', friendlySubmissionError(error), 'error')
  } finally {
    submitting.value = false
  }
}

onMounted(loadRecruitment)
</script>

<template>
  <section class="freshman-recruitment">
    <header class="freshman-page-heading">
      <div>
        <p class="freshman-eyebrow">JOIN QUANTA</p>
        <h1>加入我们</h1>
        <span>找到你感兴趣的方向，与一群认真的人共同创造。</span>
      </div>
      <div class="freshman-recruitment__step">招新通道 <strong>已开放</strong></div>
    </header>

    <nav class="freshman-tabs" aria-label="招新功能">
      <button :class="{ active: activeTab === 'about' }" @click="activeTab = 'about'">了解 Quanta</button>
      <button :class="{ active: activeTab === 'application' }" @click="activeTab = 'application'">填写报名</button>
      <button :class="{ active: activeTab === 'process' }" @click="activeTab = 'process'">查看进度</button>
    </nav>

    <p v-if="notice" class="freshman-feedback is-success" role="status">{{ notice }}</p>
    <p v-if="errorMessage" class="freshman-feedback is-error" role="alert">{{ errorMessage }}</p>

    <div v-if="activeTab === 'about'" class="freshman-about portal-card">
      <div>
        <p class="freshman-eyebrow">WHY QUANTA</p>
        <h2>把好奇心，变成真实作品</h2>
      </div>
      <div class="freshman-about__grid">
        <article><strong>01</strong><h3>跨专业协作</h3><p>产品、设计与全栈开发共同完成项目。</p></article>
        <article><strong>02</strong><h3>真实实践</h3><p>从需求到上线，经历完整的产品创作过程。</p></article>
        <article><strong>03</strong><h3>同伴成长</h3><p>通过分享、复盘和协作，让经验可被传递。</p></article>
      </div>
    </div>

    <div v-else-if="activeTab === 'application'" class="freshman-form-shell portal-card">
      <div v-if="loading" class="freshman-loading">正在加载报名信息…</div>
      <ApplicationForm
        v-else
        :initial-value="application"
        :submitting="submitting"
        @save-draft="saveDraft"
        @submit="submit"
        @invalid="showValidationErrors"
      />
    </div>

    <div v-else>
      <div v-if="loading" class="freshman-loading portal-card">正在加载面试进度…</div>
      <InterviewTimeline v-else :processes="processes" />
    </div>

    <PortalNoticeDialog
      v-model="noticeDialog.visible"
      :type="noticeDialog.type"
      :title="noticeDialog.title"
      :message="noticeDialog.message"
    />
  </section>
</template>

<style src="../freshman.css"></style>
<style src="./recruitment.css"></style>
