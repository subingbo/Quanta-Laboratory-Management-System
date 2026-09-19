<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PermissionButton from '@/components/PermissionButton.vue'
import {
  departmentLabels, exportRecruitmentList, getEvaluations, getNoticePreview,
  getRecruitmentApplication, getRecruitmentApplications, getRecruitmentStatistics,
  saveEvaluation, saveInterviewDecision, saveInterviewScore, sendResultNotice,
} from '@/api/recruitment'
import { saveBlob } from '@/utils/download'
import { useUserStore } from '@/stores/user'
import CandidateTable from './components/CandidateTable.vue'
import DecisionDialog from './components/DecisionDialog.vue'
import FeedbackDialog from './components/FeedbackDialog.vue'
import NoticeDialog from './components/NoticeDialog.vue'
import RecruitmentBoard from './components/RecruitmentBoard.vue'
import RecruitmentFilters from './components/RecruitmentFilters.vue'
import RecruitmentTabs from './components/RecruitmentTabs.vue'
import ResumeDialog from './components/ResumeDialog.vue'
import ScoreDialog from './components/ScoreDialog.vue'
import { createDepartmentFileCache } from './department-file-cache'
import './recruitment.css'

const userStore = useUserStore()
const { user, permissions, roles, departmentCode } = storeToRefs(userStore)
const activeTab = ref('board')
const loading = ref(false)
const statisticsLoading = ref(false)
const rows = ref([])
const total = ref(0)
const statistics = ref({})
const loadFailed = ref(false)
const pendingAction = ref('')
const filterDraft = reactive({ department: '', keyword: '' })
const filters = reactive({ department: '', keyword: '' })
let requestSequence = 0

const hasPermission = (permission) => permissions.value.includes('*:*:*') || permissions.value.includes(permission)
const canEvaluate = computed(() => hasPermission('qt:interview:admin:evaluate'))
const canViewAllDepartments = computed(
  () => hasPermission('*:*:*') || (roles.value || []).some((role) => role === 'ceo' || role === 'admin'),
)
const currentDepartment = departmentCode
const roundId = computed(() => Number(activeTab.value) || 1)
const departmentOptions = computed(() => Object.entries(departmentLabels).map(([value, label]) => ({ value, label })))
const filteredRows = computed(() => {
  const keyword = filters.keyword.trim().toLocaleLowerCase()
  return rows.value.filter((row) => {
    const matchesDepartment = !filters.department || row.choices?.some((choice) => choice.department === filters.department)
    const searchable = `${row.studentNo || ''} ${row.name || row.realName || ''}`.toLocaleLowerCase()
    return matchesDepartment && (!keyword || searchable.includes(keyword))
  })
})

const resume = reactive({ visible: false, loading: false, application: null })
const feedback = reactive({ visible: false, mode: 'view', loading: false, submitting: false, row: null, department: '', options: [], content: '', evaluations: [] })
const decision = reactive({ visible: false, submitting: false, row: null, department: '', options: [], result: 'PASS' })
const score = reactive({ visible: false, submitting: false, row: null, department: '', options: [], value: null, updatedBy: '', updatedTime: '' })
const notice = reactive({ visible: false, loading: false, submitting: false, row: null, preview: {}, cachedFile: null })
const noticeQrCache = createDepartmentFileCache()

function eligibleTracks(row, targetRound = roundId.value) {
  return (row?.choices || []).filter((track) => {
    if (targetRound === 2 && !track.rounds?.[2]?.advanced) return false
    return canViewAllDepartments.value || track.department === currentDepartment.value
  })
}
function trackOptions(row, targetRound = roundId.value) {
  return eligibleTracks(row, targetRound).map((track) => ({ value: track.department, label: `${track.choiceOrder === 1 ? '第一志愿' : '第二志愿'} · ${departmentLabels[track.department] || track.department}` }))
}
function findTrack(row, department) { return row?.choices?.find((track) => track.department === department) }

async function loadStatistics() {
  statisticsLoading.value = true
  try { statistics.value = await getRecruitmentStatistics() }
  catch (error) { ElMessage.error(error.message || '招聘统计加载失败') }
  finally { statisticsLoading.value = false }
}
async function loadList() {
  if (activeTab.value === 'board') return
  const currentRequest = ++requestSequence
  loading.value = true
  loadFailed.value = false
  try {
    const result = await getRecruitmentApplications({ roundId: roundId.value, pageNum: 1, pageSize: 50 })
    if (currentRequest !== requestSequence) return
    rows.value = roundId.value === 2 ? result.rows.filter((row) => row.choices?.some((track) => track.rounds?.[2]?.advanced)) : result.rows
    total.value = result.total
  } catch (error) {
    if (currentRequest !== requestSequence) return
    loadFailed.value = true
    ElMessage.error(error.message || '招新名单加载失败')
  } finally { if (currentRequest === requestSequence) loading.value = false }
}
async function refreshAll() { await Promise.all([loadStatistics(), loadList()]) }
function applyFilters() { filters.department = filterDraft.department; filters.keyword = filterDraft.keyword }
function resetFilters() { filterDraft.department = ''; filterDraft.keyword = ''; applyFilters() }

async function openResume(row) {
  resume.visible = true; resume.loading = true; resume.application = row
  try { resume.application = await getRecruitmentApplication(row.applicationId) }
  catch (error) { ElMessage.error(error.message || '简历加载失败') }
  finally { resume.loading = false }
}
async function loadFeedback() {
  feedback.loading = true
  try {
    feedback.evaluations = await getEvaluations({
      applicationId: feedback.row.applicationId,
      roundId: 1,
      department: feedback.department,
    })
    if (feedback.mode === 'edit') {
      const mine = feedback.evaluations.find(
        (item) => (item.evaluatorUserId || item.interviewerId) === user.value?.userId,
      )
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
async function prepareFeedback(row, mode) {
  feedback.mode = mode; feedback.row = row; feedback.options = trackOptions(row, 1)
  feedback.department = mode === 'edit' ? currentDepartment.value : feedback.options[0]?.value || ''
  feedback.content = ''; feedback.evaluations = []; feedback.visible = true
  if (feedback.department) await loadFeedback()
}
const openViewFeedback = (row) => prepareFeedback(row, 'view')
const openEditFeedback = (row) => prepareFeedback(row, 'edit')
async function changeFeedbackDepartment(department) { feedback.department = department; await loadFeedback() }
async function submitFeedback() {
  if (!feedback.content.trim()) return ElMessage.warning('请输入面评内容')
  feedback.submitting = true
  try {
    await saveEvaluation({ applicationId: feedback.row.applicationId, roundId: 1, department: feedback.department, content: feedback.content })
    ElMessage.success('面评已保存'); feedback.visible = false; await loadList()
  } catch (error) { ElMessage.error(error.message || '面评保存失败') }
  finally { feedback.submitting = false }
}

function openResultDecision(row, status) {
  const options = trackOptions(row, roundId.value)
  if (!options.length) return ElMessage.warning('当前没有可评定的志愿部门')
  decision.result = status; decision.row = row; decision.department = options[0].value; decision.options = options; decision.visible = true
}
async function submitDecision() {
  const track = findTrack(decision.row, decision.department)
  if (roundId.value === 2 && track?.rounds?.[2]?.score == null) return ElMessage.warning('请先填写该部门的二面分数')
  decision.submitting = true; pendingAction.value = `decision-${decision.row.applicationId}`
  try {
    await saveInterviewDecision(decision.row.applicationId, roundId.value, decision.department, decision.result)
    ElMessage.success(`${departmentLabels[decision.department] || decision.department} 已设为 ${decision.result === 'PASS' ? 'Pass' : 'Out'}`)
    decision.visible = false; await refreshAll()
  } catch (error) { ElMessage.error(error.message || '评定失败') }
  finally { decision.submitting = false; pendingAction.value = '' }
}

function syncScoreTrack() {
  const state = findTrack(score.row, score.department)?.rounds?.[2] || {}
  score.value = state.score ?? null; score.updatedBy = state.updatedBy || ''; score.updatedTime = state.updatedTime || ''
}
function openScore(row) {
  score.row = row; score.options = trackOptions(row, 2); score.department = score.options[0]?.value || ''; syncScoreTrack(); score.visible = true
}
function changeScoreDepartment(department) { score.department = department; syncScoreTrack() }
const canEditScore = computed(() => canEvaluate.value && score.department === currentDepartment.value && !['PASS', 'OUT'].includes(findTrack(score.row, score.department)?.rounds?.[2]?.status))
async function submitScore(value) {
  score.submitting = true
  try { await saveInterviewScore(score.row.applicationId, score.department, value); ElMessage.success('二面评分已保存'); score.visible = false; await loadList() }
  catch (error) { ElMessage.error(error.message || '评分保存失败') }
  finally { score.submitting = false }
}

async function openNotice(row) {
  notice.row = row; notice.preview = {}; notice.cachedFile = null; notice.visible = true; notice.loading = true
  try {
    notice.preview = await getNoticePreview(row.applicationId)
    notice.cachedFile = noticeQrCache.get(notice.preview.offeredDepartment)
  }
  catch (error) { ElMessage.error(error.message || '邮件模板加载失败'); notice.visible = false }
  finally { notice.loading = false }
}
function updateNoticeQrCache(file) {
  const department = notice.preview.offeredDepartment
  if (!department) return
  if (file) noticeQrCache.set(department, file)
  else noticeQrCache.clear(department)
  notice.cachedFile = file
}
async function submitNotice(qrCode) {
  notice.submitting = true; pendingAction.value = `notice-${notice.row.applicationId}`
  try { await sendResultNotice(notice.row.applicationId, qrCode); ElMessage.success('结果邮件已发送'); notice.visible = false; await refreshAll() }
  catch (error) { ElMessage.error(error.message || '邮件发送失败') }
  finally { notice.submitting = false; pendingAction.value = '' }
}

async function exportList() {
  try { const result = await exportRecruitmentList({ roundId: roundId.value, department: filters.department, keyword: filters.keyword }); saveBlob(result.blob, result.fileName); ElMessage.success('下载成功') }
  catch (error) { ElMessage.error(error.message || '导出失败') }
}
watch(activeTab, () => { if (activeTab.value !== 'board') loadList() })
onMounted(refreshAll)
</script>

<template>
  <div class="recruitment-view">
    <section class="page-card recruitment-card">
      <div class="recruitment-card__toolbar">
        <RecruitmentTabs v-model="activeTab" />
        <ElTooltip v-if="activeTab !== 'board'" content="导出的内容是当前页面筛选结果" placement="top" :disabled="activeTab !== '2'">
          <PermissionButton size="small" class="quanta-primary-button" :icon="Download" :permissions="'qt:interview:admin:export'" @click="exportList">导出</PermissionButton>
        </ElTooltip>
      </div>
      <RecruitmentBoard v-if="activeTab === 'board'" :statistics="statistics" :loading="statisticsLoading" />
      <div v-else-if="loadFailed && !loading" class="recruitment-card__error"><span>招新名单暂时无法加载。</span><ElButton type="primary" link @click="loadList">重新加载</ElButton></div>
      <template v-else>
        <RecruitmentFilters v-model:department="filterDraft.department" v-model:keyword="filterDraft.keyword" :departments="departmentOptions" :visible-count="filteredRows.length" :total-count="rows.length" @search="applyFilters" @reset="resetFilters" />
        <CandidateTable :rows="filteredRows" :loading="loading" :round-id="roundId" :pending-action="pendingAction" @resume="openResume" @view-feedback="openViewFeedback" @edit-feedback="openEditFeedback" @select-result="openResultDecision" @score="openScore" @notice="openNotice" />
      </template>
    </section>
    <ResumeDialog v-model="resume.visible" :application="resume.application" :loading="resume.loading" />
    <FeedbackDialog v-model="feedback.visible" :mode="feedback.mode" :candidate-name="feedback.row?.name" :round-id="1" :department="feedback.department" :options="feedback.options" :content="feedback.content" :evaluations="feedback.evaluations" :loading="feedback.loading" :submitting="feedback.submitting" :can-switch="canViewAllDepartments && feedback.mode === 'view'" @update:department="changeFeedbackDepartment" @update:content="feedback.content = $event" @submit="submitFeedback" />
    <DecisionDialog v-model="decision.visible" :candidate-name="decision.row?.name" :round-id="roundId" :result="decision.result" :department="decision.department" :options="decision.options" :submitting="decision.submitting" @update:department="decision.department = $event" @confirm="submitDecision" />
    <ScoreDialog v-model="score.visible" :candidate-name="score.row?.name" :department="score.department" :options="score.options" :score="score.value" :can-edit="canEditScore" :updated-by="score.updatedBy" :updated-time="score.updatedTime" :submitting="score.submitting" @update:department="changeScoreDepartment" @submit="submitScore" />
    <NoticeDialog v-model="notice.visible" :preview="notice.preview" :cached-file="notice.cachedFile" :loading="notice.loading" :submitting="notice.submitting" @update:cached-file="updateNoticeQrCache" @submit="submitNotice" />
  </div>
</template>
