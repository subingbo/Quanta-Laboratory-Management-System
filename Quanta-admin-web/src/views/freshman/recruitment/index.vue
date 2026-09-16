<script setup>
import { onMounted, ref } from 'vue'
import {
  getMyApplication,
  getMyInterviewProcess,
  submitApplication,
} from '@/api/portal/recruitment'
import {
  clearRecruitmentDraft,
  loadRecruitmentDraft,
  saveRecruitmentDraft,
} from '@/utils/recruitment-draft'
import ApplicationForm, { emptyApplication } from './components/ApplicationForm.vue'
import InterviewTimeline from './components/InterviewTimeline.vue'

const activeTab = ref('application')
const loading = ref(true)
const submitting = ref(false)
const application = ref(emptyApplication())
const processes = ref([])
const notice = ref('')
const errorMessage = ref('')

async function loadRecruitment() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [serverApplication, interviewProcesses] = await Promise.all([
      getMyApplication(),
      getMyInterviewProcess(),
    ])
    application.value = {
      ...emptyApplication(),
      ...(serverApplication || {}),
      ...(loadRecruitmentDraft() || {}),
    }
    processes.value = interviewProcesses
  } catch (error) {
    application.value = { ...emptyApplication(), ...(loadRecruitmentDraft() || {}) }
    errorMessage.value = error.message || '招新信息加载失败'
  } finally {
    loading.value = false
  }
}

function saveDraft(form) {
  saveRecruitmentDraft(form)
  application.value = { ...form }
  notice.value = '草稿已保存在当前浏览器'
  errorMessage.value = ''
}

async function submit(form) {
  if (submitting.value) return
  submitting.value = true
  notice.value = ''
  errorMessage.value = ''
  try {
    await submitApplication(form)
    clearRecruitmentDraft()
    notice.value = '报名提交成功'
    application.value = { ...form }
    try {
      processes.value = await getMyInterviewProcess()
    } catch {
      errorMessage.value = '报名已提交，但进度刷新失败，请稍后在“查看进度”中重试。'
    }
  } catch (error) {
    saveRecruitmentDraft(form)
    application.value = { ...form }
    errorMessage.value = error.message || '报名提交失败'
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
      />
    </div>

    <div v-else>
      <div v-if="loading" class="freshman-loading portal-card">正在加载面试进度…</div>
      <InterviewTimeline v-else :processes="processes" />
    </div>
  </section>
</template>

<style src="../freshman.css"></style>
<style src="./recruitment.css"></style>
