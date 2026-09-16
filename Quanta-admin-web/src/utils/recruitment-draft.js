const RECRUITMENT_DRAFT_KEY = 'quanta_recruitment_draft'

export function loadRecruitmentDraft() {
  const stored = localStorage.getItem(RECRUITMENT_DRAFT_KEY)
  if (!stored) return null
  try {
    const value = JSON.parse(stored)
    if (!value || typeof value !== 'object' || Array.isArray(value)) throw new Error('invalid draft')
    return value
  } catch {
    localStorage.removeItem(RECRUITMENT_DRAFT_KEY)
    return null
  }
}

export function saveRecruitmentDraft(form) {
  const draft = { ...form }
  delete draft.photoFile
  localStorage.setItem(RECRUITMENT_DRAFT_KEY, JSON.stringify(draft))
}

export function clearRecruitmentDraft() {
  localStorage.removeItem(RECRUITMENT_DRAFT_KEY)
}
