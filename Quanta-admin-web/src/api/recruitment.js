import { request } from '@/utils/request'
import { requestBlob } from '@/utils/download'

export const departmentLabels = {
  BACKEND: '后端部',
  PRODUCT: '产品部',
  DESIGN: '设计部',
  FRONTEND: '前端部',
  ANDROID: '安卓部',
}

export const resultLabels = {
  PENDING: '待评',
  PASS: 'Pass',
  FAIL: 'Out',
}

function normalizeResultStatus(status) {
  return status === 'OUT' ? 'FAIL' : status || 'PENDING'
}

function normalizeTrack(track = {}) {
  return {
    ...track,
    label: departmentLabels[track.department] || track.department,
    rounds: Object.fromEntries(
      Object.entries(track.rounds || {}).map(([round, value]) => [
        round,
        { ...value, status: normalizeResultStatus(value?.status) },
      ]),
    ),
  }
}

function buildChoice(application, order) {
  const prefix = order === 1 ? 'firstChoice' : 'secondChoice'
  const department = application[prefix]
  if (!department) return null
  const status = application[`${prefix}Status`]
  const firstRoundStatus = application[`${prefix}FirstRoundStatus`] ?? status
  const secondRoundStatus = application[`${prefix}SecondRoundStatus`] ?? status
  return {
    choiceOrder: order,
    department,
    label: departmentLabels[department] || department,
    rounds: {
      1: { status: normalizeResultStatus(firstRoundStatus), evaluations: [] },
      2: {
        status: normalizeResultStatus(secondRoundStatus),
        advanced: false,
        evaluations: [],
      },
    },
  }
}

function applyDefaultAdvancement(choices) {
  const first = choices.find((item) => item.choiceOrder === 1)
  const second = choices.find((item) => item.choiceOrder === 2)
  if (first?.rounds?.[1]?.status === 'PASS') first.rounds[2].advanced = true
  else if (second?.rounds?.[1]?.status === 'PASS') second.rounds[2].advanced = true
  return choices
}

export function mapApplication(application = {}) {
  const choices = application.tracks?.length
    ? application.tracks.map(normalizeTrack)
    : applyDefaultAdvancement([buildChoice(application, 1), buildChoice(application, 2)].filter(Boolean))
  return {
    ...application,
    id: application.applicationId,
    name: application.realName || '-',
    phone: application.phonenumber || application.phone || '-',
    resumeUrl: application.resumeAccessUrl || application.resumeUrl || '',
    resumeFileName: application.resumeFileName || '',
    appliedAt: application.createTime || application.appliedAt || '-',
    applicationStatus: application.applyStatus || application.applicationStatus,
    choices,
  }
}

export function mergeApplicationDetail(payload = {}) {
  return mapApplication({ ...(payload.application || payload), ...(payload.profile || {}) })
}

export function createOfferPayload(data = {}) {
  return {
    applicationId: data.applicationId,
    department: data.department,
    decision: data.decision === 'FAIL' ? 'OUT' : data.decision,
    notice: data.notice ?? data.content,
    roundId: data.roundId ?? 2,
  }
}

export async function getRecruitmentStatistics() {
  const response = await request({ url: '/qt/interview/admin/statistics', method: 'get' })
  const data = response.data || {}
  return {
    ...data,
    submissions: data.submissions ?? data.submittedCount,
    firstPassed: data.firstPassed ?? data.processingCount,
    secondPassed: data.secondPassed ?? data.offeredCount,
    joined: data.joined ?? data.joinedCount,
  }
}

export async function getRecruitmentApplications(query = {}) {
  const response = await request({
    url: '/qt/interview/admin/applications',
    method: 'get',
    params: query,
  })
  return {
    rows: (response.rows || []).map(mapApplication),
    total: Number(response.total) || 0,
  }
}

export async function getRecruitmentApplication(applicationId) {
  const response = await request({
    url: `/qt/interview/admin/applications/${applicationId}`,
    method: 'get',
  })
  return mergeApplicationDetail(response.data || {})
}

export function mapEvaluation(item = {}) {
  const evaluatorUserId = item.evaluatorUserId ?? item.interviewerId
  const evaluatorUserName = item.evaluatorUserName || item.interviewerName || ''
  return {
    ...item,
    evaluatorUserId,
    evaluatorUserName,
    interviewerId: evaluatorUserId,
    interviewerName: evaluatorUserName,
  }
}

export async function getEvaluations(params) {
  const response = await request({
    url: '/qt/interview/admin/evaluations',
    method: 'get',
    params,
  })
  const rows = Array.isArray(response.data) ? response.data : response.data?.rows || response.rows || []
  return rows.map(mapEvaluation)
}

export function saveEvaluation(data) {
  return request({ url: '/qt/interview/admin/evaluations', method: 'post', data })
}

export function saveInterviewResult(data) {
  return request({ url: '/qt/interview/result', method: 'post', data })
}

export function sendOffer(data) {
  return request({
    url: '/qt/interview/admin/offers',
    method: 'post',
    data: createOfferPayload(data),
  })
}

export function exportRecruitmentList(params) {
  return requestBlob(
    {
      url: '/qt/interview/admin/applications/export',
      method: 'post',
      params,
    },
    '招新名单.xlsx',
  )
}
