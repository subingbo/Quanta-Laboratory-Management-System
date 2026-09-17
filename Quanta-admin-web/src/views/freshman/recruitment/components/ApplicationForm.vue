<script>
export function emptyApplication() {
  return {
    realName: '',
    gender: '',
    className: '',
    firstChoice: '',
    secondChoice: '',
    selfIntro: '',
    codingExperience: '',
    codingExperienceDesc: '',
    quantaUnderstanding: '',
    photoUrl: '',
    storedPhotoUrl: '',
    photoFile: null,
    resumeUrl: '',
    storedResumeUrl: '',
    resumeFileName: '',
    resumeFile: null,
  }
}
</script>

<script setup>
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { recruitmentDepartments } from '@/api/portal/recruitment'

const props = defineProps({
  initialValue: {
    type: Object,
    default: emptyApplication,
  },
  submitting: Boolean,
})
const emit = defineEmits(['submit', 'save-draft', 'invalid'])
const model = reactive(emptyApplication())
const formRef = ref(null)
const fieldErrors = reactive({})
const photoInput = ref(null)
const resumeInput = ref(null)
const photoError = ref('')
const resumeError = ref('')
const photoPreviewFailed = ref(false)
let localPhotoUrl = ''

const allowedPhotoTypes = new Set(['image/jpeg', 'image/png'])
const allowedPhotoExtensions = new Set(['jpg', 'jpeg', 'png'])
const maxPhotoSize = 5 * 1024 * 1024
const maxResumeSize = 10 * 1024 * 1024
const hasPhotoPreview = computed(() => Boolean(model.photoUrl) && !photoPreviewFailed.value)
const photoFileName = computed(() => {
  if (model.photoFile?.name) return model.photoFile.name
  if (photoPreviewFailed.value) return '原证件照暂时无法预览，请重新选择'
  if (model.photoUrl || model.storedPhotoUrl) return '已上传证件照'
  return '尚未选择照片'
})
const resumeFileName = computed(() => {
  if (model.resumeFile?.name) return model.resumeFile.name
  if (model.resumeFileName) return model.resumeFileName
  if (model.resumeUrl || model.storedResumeUrl) return '已上传 PDF 简历'
  return '尚未选择 PDF 简历'
})

watch(
  () => props.initialValue,
  (value) => {
    Object.assign(model, emptyApplication(), value || {})
    photoError.value = ''
    resumeError.value = ''
    photoPreviewFailed.value = false
    if (model.photoFile instanceof Blob) setLocalPhotoPreview(model.photoFile)
  },
  { immediate: true, deep: true },
)

function selectPhoto(event) {
  const file = event.target.files?.[0]
  if (!file) return
  const extension = file.name.split('.').pop()?.toLowerCase() || ''
  if (!allowedPhotoTypes.has(file.type) && !allowedPhotoExtensions.has(extension)) {
    photoError.value = '请选择 JPG 或 PNG 格式的图片'
    event.target.value = ''
    return
  }
  if (file.size > maxPhotoSize) {
    photoError.value = '证件照大小不能超过 5 MB'
    event.target.value = ''
    return
  }

  photoError.value = ''
  photoPreviewFailed.value = false
  model.photoFile = file
  setLocalPhotoPreview(file)
}

function setLocalPhotoPreview(file) {
  if (localPhotoUrl && typeof URL.revokeObjectURL === 'function') URL.revokeObjectURL(localPhotoUrl)
  if (typeof URL.createObjectURL === 'function') {
    localPhotoUrl = URL.createObjectURL(file)
    model.photoUrl = localPhotoUrl
  }
}

function openPhotoPicker() {
  photoInput.value?.click()
}

function selectResume(event) {
  const file = event.target.files?.[0]
  if (!file) return
  const extension = file.name.split('.').pop()?.toLowerCase() || ''
  if (file.type !== 'application/pdf' && extension !== 'pdf') {
    resumeError.value = '请选择 PDF 格式的简历'
    event.target.value = ''
    return
  }
  if (file.size > maxResumeSize) {
    resumeError.value = 'PDF 简历大小不能超过 10 MB'
    event.target.value = ''
    return
  }

  resumeError.value = ''
  model.resumeFile = file
  model.resumeFileName = file.name
}

function openResumePicker() {
  resumeInput.value?.click()
}

function handlePhotoError() {
  photoPreviewFailed.value = true
}

onBeforeUnmount(() => {
  if (localPhotoUrl && typeof URL.revokeObjectURL === 'function') URL.revokeObjectURL(localPhotoUrl)
})

function snapshot() {
  return { ...model }
}

function validateApplication() {
  const errors = {}
  const requiredText = [
    ['realName', '姓名'],
    ['gender', '性别'],
    ['className', '班级'],
    ['firstChoice', '第一志愿'],
    ['secondChoice', '第二志愿'],
    ['selfIntro', '自我介绍'],
    ['codingExperience', '编程经验'],
    ['quantaUnderstanding', '对 Quanta 的了解'],
  ]
  requiredText.forEach(([field, label]) => {
    if (!String(model[field] || '').trim()) errors[field] = label
  })
  if (!model.photoFile && !model.photoUrl && !model.storedPhotoUrl) errors.photo = '证件照'
  if (!model.resumeFile && !model.resumeUrl && !model.storedResumeUrl) errors.resume = 'PDF 简历'
  if (model.firstChoice && model.firstChoice === model.secondChoice) {
    errors.secondChoice = '第一、第二志愿不能相同'
  }
  if (model.codingExperience === '1' && !model.codingExperienceDesc.trim()) {
    errors.codingExperienceDesc = '编程经历说明'
  }
  return errors
}

function handleSubmit() {
  const errors = validateApplication()
  Object.keys(fieldErrors).forEach((key) => delete fieldErrors[key])
  Object.assign(fieldErrors, errors)
  const missingFields = Object.values(errors)
  if (missingFields.length) {
    const firstField = Object.keys(errors)[0]
    const selector = firstField === 'photo'
      ? '[data-testid="photo-upload-card"]'
      : firstField === 'resume'
        ? '[data-testid="resume-upload-card"]'
        : `[name="${firstField}"]`
    formRef.value?.querySelector(selector)?.focus?.()
    emit('invalid', missingFields)
    return
  }
  emit('submit', snapshot())
}
</script>

<template>
  <form ref="formRef" class="application-form application-grid" novalidate @submit.prevent="handleSubmit">
    <label :class="{ 'is-invalid': fieldErrors.realName }">
      <span>姓名</span>
      <input v-model.trim="model.realName" name="realName" autocomplete="name" required />
      <small v-if="fieldErrors.realName" class="application-form__field-error">请填写姓名</small>
    </label>
    <label :class="{ 'is-invalid': fieldErrors.gender }">
      <span>性别</span>
      <select v-model="model.gender" name="gender" required>
        <option value="" disabled>请选择</option>
        <option value="男">男</option>
        <option value="女">女</option>
      </select>
      <small v-if="fieldErrors.gender" class="application-form__field-error">请选择性别</small>
    </label>
    <label class="application-grid__wide" :class="{ 'is-invalid': fieldErrors.className }">
      <span>班级</span>
      <input v-model.trim="model.className" name="className" placeholder="例如：软工2402" required />
      <small v-if="fieldErrors.className" class="application-form__field-error">请填写班级</small>
    </label>
    <label :class="{ 'is-invalid': fieldErrors.firstChoice }">
      <span>第一志愿</span>
      <select v-model="model.firstChoice" name="firstChoice" required>
        <option value="" disabled>请选择</option>
        <option v-for="department in recruitmentDepartments" :key="department.value" :value="department.value">
          {{ department.label }}
        </option>
      </select>
      <small v-if="fieldErrors.firstChoice" class="application-form__field-error">{{ fieldErrors.firstChoice }}</small>
    </label>
    <label :class="{ 'is-invalid': fieldErrors.secondChoice }">
      <span>第二志愿</span>
      <select v-model="model.secondChoice" name="secondChoice" required>
        <option value="" disabled>请选择</option>
        <option v-for="department in recruitmentDepartments" :key="department.value" :value="department.value">
          {{ department.label }}
        </option>
      </select>
      <small v-if="fieldErrors.secondChoice" class="application-form__field-error">{{ fieldErrors.secondChoice }}</small>
    </label>
    <div class="application-grid__wide application-form__photo-field" :class="{ 'is-invalid': fieldErrors.photo }">
      <span class="application-form__field-label">证件照 <em>必填</em></span>
      <div class="application-form__photo-card" data-testid="photo-upload-card">
        <div class="application-form__photo-preview" :class="{ 'has-photo': hasPhotoPreview }">
          <img
            v-if="hasPhotoPreview"
            :src="model.photoUrl"
            alt="证件照预览"
            @error="handlePhotoError"
          />
          <div v-else class="application-form__photo-placeholder" aria-hidden="true">
            <svg viewBox="0 0 48 48" role="img">
              <circle cx="24" cy="17" r="8" />
              <path d="M10 42c1.5-9 6.2-13.5 14-13.5S36.5 33 38 42" />
            </svg>
            <strong>3:4</strong>
            <span>证件照预览</span>
          </div>
        </div>
        <div class="application-form__photo-copy">
          <div>
            <p class="application-form__photo-kicker">PHOTO ID</p>
            <h3>上传一张清晰的证件照</h3>
            <p>照片仅用于本次招新资料与面试身份核对。</p>
          </div>
          <div class="application-form__photo-rules" aria-label="上传要求">
            <span>JPG / PNG</span>
            <span>最大 5 MB</span>
          </div>
          <p class="application-form__photo-name" data-testid="photo-file-name">
            <i aria-hidden="true"></i>{{ photoFileName }}
          </p>
          <p v-if="photoError" class="application-form__photo-error" data-testid="photo-error" role="alert">
            {{ photoError }}
          </p>
          <input
            ref="photoInput"
            class="sr-only"
            name="photoFile"
            type="file"
            accept="image/jpeg,image/png,.jpg,.jpeg,.png"
            :required="!model.storedPhotoUrl && !model.photoUrl"
            @change="selectPhoto"
          />
          <button type="button" class="application-form__photo-button" @click="openPhotoPicker">
            <span aria-hidden="true">＋</span>{{ model.photoUrl ? '重新选择照片' : '选择照片' }}
          </button>
        </div>
      </div>
      <small v-if="fieldErrors.photo" class="application-form__field-error">请上传证件照</small>
    </div>
    <div class="application-grid__wide application-form__resume-field" :class="{ 'is-invalid': fieldErrors.resume }">
      <span class="application-form__field-label">PDF 简历 <em>必填</em></span>
      <div class="application-form__resume-card" data-testid="resume-upload-card">
        <div class="application-form__resume-icon" aria-hidden="true">
          <svg viewBox="0 0 48 48">
            <path d="M12 4h17l9 9v31H12z" />
            <path d="M29 4v10h9M18 24h14M18 30h14M18 36h9" />
          </svg>
          <strong>PDF</strong>
        </div>
        <div class="application-form__resume-copy">
          <div>
            <h3>上传你的 PDF 简历</h3>
            <p>用于补充展示项目经历、技能与作品信息。</p>
          </div>
          <div class="application-form__photo-rules" aria-label="PDF 上传要求">
            <span>仅 PDF</span>
            <span>最大 10 MB</span>
          </div>
          <p class="application-form__photo-name" data-testid="resume-file-name">
            <i aria-hidden="true"></i>{{ resumeFileName }}
          </p>
          <p v-if="resumeError" class="application-form__photo-error" data-testid="resume-error" role="alert">
            {{ resumeError }}
          </p>
          <input
            ref="resumeInput"
            class="sr-only"
            name="resumeFile"
            type="file"
            accept="application/pdf,.pdf"
            @change="selectResume"
          />
          <button type="button" class="application-form__resume-button" @click="openResumePicker">
            {{ model.resumeFile || model.resumeUrl || model.storedResumeUrl ? '重新选择 PDF' : '选择 PDF 简历' }}
          </button>
        </div>
      </div>
      <small v-if="fieldErrors.resume" class="application-form__field-error">请上传 PDF 简历</small>
    </div>
    <label class="application-grid__wide" :class="{ 'is-invalid': fieldErrors.selfIntro }">
      <span>自我介绍</span>
      <textarea v-model.trim="model.selfIntro" name="selfIntro" rows="5" required></textarea>
      <small v-if="fieldErrors.selfIntro" class="application-form__field-error">请填写自我介绍</small>
    </label>
    <fieldset class="application-grid__wide" :class="{ 'is-invalid': fieldErrors.codingExperience }">
      <legend>是否有编程经验</legend>
      <label class="application-form__radio"><input v-model="model.codingExperience" type="radio" value="1" />有</label>
      <label class="application-form__radio"><input v-model="model.codingExperience" type="radio" value="0" />暂无</label>
      <small v-if="fieldErrors.codingExperience" class="application-form__field-error">请选择是否有编程经验</small>
    </fieldset>
    <label v-if="model.codingExperience === '1'" class="application-grid__wide" :class="{ 'is-invalid': fieldErrors.codingExperienceDesc }">
      <span>编程经验</span>
      <textarea v-model.trim="model.codingExperienceDesc" name="codingExperienceDesc" rows="4"></textarea>
      <small v-if="fieldErrors.codingExperienceDesc" class="application-form__field-error">请填写编程经历说明</small>
    </label>
    <label class="application-grid__wide" :class="{ 'is-invalid': fieldErrors.quantaUnderstanding }">
      <span>你对 Quanta 的了解</span>
      <textarea v-model.trim="model.quantaUnderstanding" name="quantaUnderstanding" rows="4" required></textarea>
      <small v-if="fieldErrors.quantaUnderstanding" class="application-form__field-error">请填写对 Quanta 的了解</small>
    </label>
    <div class="application-form__actions application-grid__wide">
      <button type="button" class="portal-secondary-button" :disabled="submitting" @click="emit('save-draft', snapshot())">
        保存草稿
      </button>
      <button type="submit" class="portal-primary-button" :disabled="submitting">
        {{ submitting ? '提交中…' : '提交报名' }}
      </button>
    </div>
  </form>
</template>
