<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PermissionButton from '@/components/PermissionButton.vue'
import {
  departmentLabels,
  exportRecruitmentList,
  getEvaluations,
  getRecruitmentApplication,
  getRecruitmentApplications,
  getRecruitmentStatistics,
  saveEvaluation,
  saveInterviewResult,
  sendOffer,
} from '@/api/recruitment'
import { saveBlob } from '@/utils/download'
import { isCeoRole } from '@/composables/usePermission'
import { useUserStore } from '@/stores/user'
import CandidateTable from './components/CandidateTable.vue'
import DecisionDialog from './components/DecisionDialog.vue'
import FeedbackDialog from './components/FeedbackDialog.vue'
import OfferConfirmDialog from './components/OfferConfirmDialog.vue'
import OfferDialog from './components/OfferDialog.vue'
import RecruitmentBoard from './components/RecruitmentBoard.vue'
import RecruitmentTabs from './components/RecruitmentTabs.vue'
import ResumeDialog from './components/ResumeDialog.vue'
import './recruitment.css'

const userStore = useUserStore()
const { user, roles } = storeToRefs(userStore)
const activeTab = ref('board')
const loading = ref(false)
const statisticsLoading = ref(false)
const rows = ref([])
const total = ref(0)
const statistics = ref({})
const loadFailed = ref(false)
const pendingAction = ref('')
let requestSequence = 0

const isCeo = computed(() => roles.value.some(isCeoRole))
const currentDepartment = computed(
  () => user.value?.deptCode || user.value?.dept?.deptCode || '',
)
const roundId = computed(() => Number(activeTab.value) || 1)

const resume = reactive({ visible: false, loading: false, application: null })
const feedback = reactive({
  visible: false,
  mode: 'view',
  loading: false,
  submitting: false,
  row: null,
  department: '',
  options: [],
  content: '',
  evaluations: [],
})
const decision = reactive({
  visible: false,
  submitting: false,
  row: null,
  department: '',
  options: [],
  result: 'PASS',
})
const offerConfirm = reactive({ visible: false, submitting: false })
const offer = reactive({
  visible: false,
  submitting: false,
  evaluationsLoading: false,
  evaluations: [],
  row: null,
  department: '',
  options: [],
  decision: 'PASS',
  content: '',
})

function eligibleTracks(row, targetRound = roundId.value) {
  return (row?.choices || []).filter((track) => {
    if (targetRound === 2 && !track.rounds?.[2]?.advanced) return false
    return isCeo.value || track.department === currentDepartment.value
  })
}

function trackOptions(row, targetRound = roundId.value) {
  return eligibleTracks(row, targetRound).map((track) => ({
    value: track.department,
    label: `${track.choiceOrder === 1 ? '第一志愿' : '第二志愿'} · ${departmentLabels[track.department] || track.department}`,
  }))
}

async function loadStatistics() {
  statisticsLoading.value = true
  try {
    statistics.value = await getRecruitmentStatistics()
  } catch (error) {
    ElMessage.error(error.message || '招聘统计加载失败')
  } finally {
    statisticsLoading.value = false
  }
}

async function loadList() {
  if (activeTab.value === 'board') return
  const currentRequest = ++requestSequence
  loading.value = true
  loadFailed.value = false
  try {
    const result = await getRecruitmentApplications({ roundId: roundId.value, pageNum: 1, pageSize: 50 })
    if (currentRequest !== requestSequence) return
    rows.value = result.rows
    total.value = result.total
  } catch (error) {
    if (currentRequest !== requestSequence) return
    loadFailed.value = true
    ElMessage.error(error.message || '招新名单加载失败')
  } finally {
    if (currentRequest === requestSequence) loading.value = false
  }
}

async function refreshAll() {
  await Promise.all([loadStatistics(), loadList()])
}

async function openResume(row) {
  resume.visible = true
  resume.loading = true
  resume.application = row
  try {
    resume.application = await getRecruitmentApplication(row.applicationId)
  } catch (error) {
    ElMessage.error(error.message || '简历加载失败')
  } finally {
    resume.loading = false
  }
}

async function loadFeedback() {
  feedback.loading = true
  try {
    feedback.evaluations = await getEvaluations({
      applicationId: feedback.row.applicationId,
      roundId: roundId.value,
      department: feedback.department,
    })
    if (feedback.mode === 'edit') {
      const mine = feedback.evaluations.find((item) => item.interviewerId === user.value?.userId)
      feedback.content = mine?.content || ''
    } else {
      feedback.content = ''
    }
  } catch (error) {
    ElMessage.error(error.message || '面评加载失败')
  } finally {
    feedback.loading = false
  }
}

async function prepareFeedback(row) {
  feedback.row = row
  feedback.options = trackOptions(row)
  feedback.department = feedback.options[0]?.value || ''
  feedback.content = ''
  feedback.evaluations = []
  feedback.visible = true
  if (feedback.department) await loadFeedback()
}

async function openViewFeedback(row) {
  feedback.mode = 'view'
  await prepareFeedback(row)
}

async function openEditFeedback(row) {
  feedback.mode = 'edit'
  await prepareFeedback(row)
}

async function changeFeedbackDepartment(department) {
  feedback.department = department
  await loadFeedback()
}

async function submitFeedback() {
  if (!feedback.content.trim()) {
    ElMessage.warning('请输入面评内容')
    return
  }
  feedback.submitting = true
  try {
    await saveEvaluation({
      applicationId: feedback.row.applicationId,
      roundId: roundId.value,
      department: feedback.department,
      content: feedback.content,
    })
    ElMessage.success('面评已保存')
    feedback.visible = false
    await loadList()
  } catch (error) {
    ElMessage.error(error.message || '面评保存失败')
  } finally {
    feedback.submitting = false
  }
}

function selectFirstRoundResult(status) {
  decision.result = status
  decision.row = feedback.row
  decision.department = feedback.department
  decision.options = feedback.options.filter((option) => option.value === feedback.department)
  feedback.visible = false
  decision.visible = true
}

async function submitDecision() {
  decision.submitting = true
  pendingAction.value = `decision-${decision.row.applicationId}`
  try {
    await saveInterviewResult({
      applicationId: decision.row.applicationId,
      userId: decision.row.userId,
      roundId: roundId.value,
      department: decision.department,
      resultStatus: decision.result,
    })
    ElMessage.success(decision.result === 'PASS' ? '已通过' : '已淘汰')
    decision.visible = false
    await refreshAll()
  } catch (error) {
    ElMessage.error(error.message || '评定失败')
  } finally {
    decision.submitting = false
    pendingAction.value = ''
  }
}

function offerTemplate(row, result) {
  return result === 'PASS'
    ? `你好 ${row.name}，\n\n祝贺你通过了 Quanta 的面试考核！我们很高兴通知你，你已被录用。\n\n请尽快回复此消息确认是否接受。`
    : `你好 ${row.name}，\n\n感谢你参加 Quanta 的招新面试。很遗憾本次未能录用你，祝你未来学习顺利。`
}

function openOffer(row) {
  offer.row = row
  offer.options = trackOptions(row, 2)
  offer.department = offer.options[0]?.value || ''
  offer.decision = 'PASS'
  offer.content = ''
  offer.evaluations = []
  offerConfirm.visible = true
}

function continueOffer() {
  offerConfirm.visible = false
  offer.decision = 'PASS'
  offer.content = offerTemplate(offer.row, 'PASS')
  offer.visible = true
  loadOfferEvaluations()
}

async function loadOfferEvaluations() {
  offer.evaluationsLoading = true
  try {
    offer.evaluations = await getEvaluations({
      applicationId: offer.row.applicationId,
      roundId: 2,
      department: offer.department,
    })
  } catch (error) {
    offer.evaluations = []
    ElMessage.error(error.message || '二面面评加载失败')
  } finally {
    offer.evaluationsLoading = false
  }
}

function changeOfferDecision(value) {
  offer.decision = value
  offer.content = offerTemplate(offer.row, value)
}

async function submitOffer() {
  offer.submitting = true
  try {
    await sendOffer({
      applicationId: offer.row.applicationId,
      department: offer.department,
      decision: offer.decision,
      content: offer.content,
    })
    ElMessage.success(offer.decision === 'PASS' ? '录用通知已发送' : '淘汰通知已发送')
    offer.visible = false
    await refreshAll()
  } catch (error) {
    ElMessage.error(error.message || '通知发送失败')
  } finally {
    offer.submitting = false
  }
}

async function exportList() {
  try {
    const result = await exportRecruitmentList({ roundId: roundId.value })
    saveBlob(result.blob, result.fileName)
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error(error.message || '导出失败')
  }
}

watch(activeTab, (tab) => {
  if (tab === 'board') loadStatistics()
  else loadList()
})

onMounted(refreshAll)
</script>

<template>
  <div class="recruitment-view">
    <section class="page-card recruitment-card">
      <div class="recruitment-card__toolbar">
        <RecruitmentTabs v-model="activeTab" />
        <ElTooltip
          v-if="activeTab !== 'board'"
          content="导出的内容是当前页面筛选结果"
          placement="top"
          :disabled="activeTab !== '2'"
        >
          <PermissionButton
            size="small"
            class="quanta-primary-button"
            :icon="Download"
            :permissions="'qt:interview:admin:export'"
            @click="exportList"
          >
            导出
          </PermissionButton>
        </ElTooltip>
      </div>

      <RecruitmentBoard
        v-if="activeTab === 'board'"
        :statistics="statistics"
        :loading="statisticsLoading"
      />

      <div v-else-if="loadFailed && !loading" class="recruitment-card__error">
        <span>招新名单暂时无法加载。</span>
        <ElButton type="primary" link @click="loadList">重新加载</ElButton>
      </div>

      <CandidateTable
        v-else
        :rows="rows"
        :loading="loading"
        :round-id="roundId"
        :pending-action="pendingAction"
        @resume="openResume"
        @view-feedback="openViewFeedback"
        @edit-feedback="openEditFeedback"
        @offer="openOffer"
      />
    </section>

    <ResumeDialog v-model="resume.visible" :application="resume.application" :loading="resume.loading" />
    <FeedbackDialog
      v-model="feedback.visible"
      :mode="feedback.mode"
      :candidate-name="feedback.row?.name"
      :round-id="roundId"
      :department="feedback.department"
      :options="feedback.options"
      :content="feedback.content"
      :evaluations="feedback.evaluations"
      :loading="feedback.loading"
      :submitting="feedback.submitting"
      :can-switch="isCeo"
      @update:department="changeFeedbackDepartment"
      @update:content="feedback.content = $event"
      @select-result="selectFirstRoundResult"
      @submit="submitFeedback"
    />
    <DecisionDialog
      v-model="decision.visible"
      :candidate-name="decision.row?.name"
      :round-id="roundId"
      :result="decision.result"
      :department="decision.department"
      :options="decision.options"
      :submitting="decision.submitting"
      @confirm="submitDecision"
    />
    <OfferDialog
      v-model="offer.visible"
      :candidate="offer.row"
      :department="offer.department"
      :options="offer.options"
      :decision="offer.decision"
      :content="offer.content"
      :evaluations="offer.evaluations"
      :evaluations-loading="offer.evaluationsLoading"
      :submitting="offer.submitting"
      @update:decision="changeOfferDecision"
      @update:content="offer.content = $event"
      @submit="submitOffer"
    />
    <OfferConfirmDialog
      v-model="offerConfirm.visible"
      :candidate-name="offer.row?.name"
      :department="offer.department"
      :options="offer.options"
      :submitting="offerConfirm.submitting"
      @confirm="continueOffer"
    />
  </div>
</template>
