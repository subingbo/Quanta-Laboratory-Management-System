import { beforeEach, describe, expect, it } from 'vitest'
import {
  createOfferPayload,
  getRecruitmentApplications,
  getRecruitmentStatistics,
  mapApplication,
  mergeApplicationDetail,
  sendOffer,
} from '../recruitment'
import { findMockApplication, resetMockRecruitment } from '@/mock/data/recruitment'
import { setToken } from '@/utils/token'

describe('recruitment api', () => {
  beforeEach(() => {
    resetMockRecruitment()
    setToken('mock-token-product-manager')
  })

  it('maps a department-scoped application list', async () => {
    const result = await getRecruitmentApplications({ roundId: 1 })
    expect(result.total).toBeGreaterThan(0)
    expect(result.rows[0].choices[0].label).toBeTruthy()
    expect(result.rows.every((row) => row.choices.some((choice) => choice.department === 'PRODUCT'))).toBe(true)
  })

  it('returns unique candidate statistics', async () => {
    const result = await getRecruitmentStatistics()
    expect(result).toMatchObject({ submissions: 6, firstPassed: 1, secondPassed: 0 })
  })

  it('saves the second-round result with its notification', async () => {
    const response = await sendOffer({
      applicationId: 1003,
      department: 'PRODUCT',
      decision: 'PASS',
      content: '祝贺你通过面试。',
    })

    const application = findMockApplication(1003)
    expect(application.tracks[1].rounds[2].status).toBe('PASS')
    expect(application.applicationStatus).toBe('OFFERED')
    expect(response.data.emailSent).toBe(true)
  })

  it('maps flat backend choices to the stable page model', () => {
    const result = mapApplication({
      applicationId: 8,
      realName: '吴晓萌',
      firstChoice: 'DESIGN',
      secondChoice: 'PRODUCT',
      firstChoiceStatus: 'PASS',
      secondChoiceStatus: 'OUT',
      resumeAccessUrl: 'https://example.com/resume.pdf?token=x',
      resumeFileName: '吴晓萌-简历.pdf',
      createTime: '2026-08-30 10:00:00',
    })

    expect(result.choices.map((item) => item.department)).toEqual(['DESIGN', 'PRODUCT'])
    expect(result.choices[1].rounds[1].status).toBe('FAIL')
    expect(result.appliedAt).toBe('2026-08-30 10:00:00')
    expect(result.resumeUrl).toBe('https://example.com/resume.pdf?token=x')
    expect(result.resumeFileName).toBe('吴晓萌-简历.pdf')
  })

  it('merges detail sections and translates the offer contract', () => {
    expect(
      mergeApplicationDetail({
        application: { applicationId: 8, firstChoice: 'PRODUCT' },
        profile: { realName: '吴晓萌', major: '数字媒体' },
      }),
    ).toMatchObject({ applicationId: 8, name: '吴晓萌', major: '数字媒体' })

    expect(
      createOfferPayload({
        applicationId: 8,
        department: 'PRODUCT',
        decision: 'FAIL',
        content: '通知内容',
      }),
    ).toEqual({
      applicationId: 8,
      department: 'PRODUCT',
      decision: 'OUT',
      notice: '通知内容',
      roundId: 2,
    })
  })
})
