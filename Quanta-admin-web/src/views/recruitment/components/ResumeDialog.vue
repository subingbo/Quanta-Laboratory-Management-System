<script setup>
import { computed, ref, watch } from 'vue'
import { departmentLabels } from '@/api/recruitment'

const interviewSlots = [
  { value: '2026-09-22 18:30:00', date: '9月22日', time: '18:30–22:30' },
  { value: '2026-09-23 18:30:00', date: '9月23日', time: '18:30–22:30' },
]

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  application: { type: Object, default: null },
  loading: { type: Boolean, default: false },
  roundId: { type: Number, default: 1 },
  submitting: { type: Boolean, default: false },
})
const emit = defineEmits(['update:modelValue', 'save-interview-time'])
const visible = computed({ get: () => props.modelValue, set: (value) => emit('update:modelValue', value) })
const selectedInterviewTime = ref('')

watch(
  [() => props.modelValue, () => props.application?.firstRoundInterviewTime],
  ([isVisible, interviewTime]) => {
    if (isVisible) selectedInterviewTime.value = interviewTime || ''
  },
  { immediate: true },
)

function choice(order) {
  return props.application?.choices?.find((item) => item.choiceOrder === order)
}

function display(value) {
  return value || '暂无'
}

const resumeFileName = computed(() => {
  if (props.application?.resumeFileName) return props.application.resumeFileName
  const identity = [props.application?.realName || props.application?.name, props.application?.studentNo]
    .filter(Boolean)
    .join('-')
  return `${identity || '候选人'}-简历.pdf`
})

function saveInterviewTime() {
  if (!selectedInterviewTime.value || props.submitting) return
  emit('save-interview-time', selectedInterviewTime.value)
}
</script>

<template>
  <ElDialog v-model="visible" width="640" class="quanta-dialog resume-dialog" destroy-on-close>
    <template #header><strong>简历阅览 - {{ application?.name || application?.realName }}</strong></template>
    <div v-loading="loading" class="resume-dialog__body">
      <div class="resume-dialog__grid">
        <div><span>姓名 / 学号</span><strong>{{ display(application?.realName || application?.name) }}（{{ display(application?.studentNo) }}）</strong></div>
        <div><span>专业 / 班级</span><strong>{{ display(application?.major) }} - {{ display(application?.className) }}</strong></div>
        <div><span>第一志愿</span><strong>{{ departmentLabels[choice(1)?.department] || '暂无' }}</strong></div>
        <div><span>第二志愿</span><strong>{{ departmentLabels[choice(2)?.department] || '暂无' }}</strong></div>
        <div><span>邮箱</span><strong>{{ display(application?.email) }}</strong></div>
        <div><span>联系方式</span><strong>{{ display(application?.phone || application?.phonenumber) }}</strong></div>
      </div>
      <section><span>个人简介</span><p>{{ application?.selfIntro || '无' }}</p></section>
      <section><span>编程经历</span><p>{{ application?.codingExperienceDesc || '无' }}</p></section>
      <section><span>对 Quanta 的认识</span><p>{{ application?.quantaUnderstanding || '无' }}</p></section>
      <section class="resume-dialog__pdf">
        <span>PDF 简历</span>
        <div v-if="application?.resumeUrl" class="resume-dialog__pdf-card">
          <div>
            <strong>{{ resumeFileName }}</strong>
            <small>PDF 文件</small>
          </div>
          <div class="resume-dialog__pdf-actions">
            <a
              :href="application.resumeUrl"
              target="_blank"
              rel="noopener noreferrer"
              data-testid="resume-preview"
            >在线预览</a>
            <a
              :href="application.resumeUrl"
              :download="resumeFileName"
              data-testid="resume-download"
            >下载 PDF</a>
          </div>
        </div>
        <p v-else class="resume-dialog__pdf-empty">暂无 PDF 简历</p>
      </section>
      <section v-if="roundId === 1" class="resume-dialog__interview-time" data-test="interview-time-picker">
        <div class="resume-dialog__interview-heading">
          <div>
            <span>一面安排</span>
            <strong>选择面试时间</strong>
          </div>
          <small>两个志愿部门共用同一个面试时间</small>
        </div>
        <ElRadioGroup v-model="selectedInterviewTime" class="resume-dialog__slot-list">
          <ElRadio v-for="slot in interviewSlots" :key="slot.value" :value="slot.value" class="resume-dialog__slot">
            <strong>{{ slot.date }}</strong>
            <span>{{ slot.time }}</span>
          </ElRadio>
        </ElRadioGroup>
        <ElButton
          type="primary"
          class="resume-dialog__save-time"
          data-test="save-interview-time"
          :disabled="!selectedInterviewTime"
          :loading="submitting"
          @click="saveInterviewTime"
        >
          保存面试时间
        </ElButton>
      </section>
    </div>
  </ElDialog>
</template>
