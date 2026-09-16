<script>
export function emptyApplication() {
  return {
    realName: '',
    gender: '',
    className: '',
    firstChoice: '',
    secondChoice: '',
    selfIntro: '',
    codingExperience: '0',
    codingExperienceDesc: '',
    quantaUnderstanding: '',
    photoUrl: '',
    storedPhotoUrl: '',
    photoFile: null,
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
const emit = defineEmits(['submit', 'save-draft'])
const model = reactive(emptyApplication())
const photoInput = ref(null)
const photoError = ref('')
const photoPreviewFailed = ref(false)
let localPhotoUrl = ''

const allowedPhotoTypes = new Set(['image/jpeg', 'image/png', 'image/webp'])
const maxPhotoSize = 5 * 1024 * 1024
const hasPhotoPreview = computed(() => Boolean(model.photoUrl) && !photoPreviewFailed.value)
const photoFileName = computed(() => {
  if (model.photoFile?.name) return model.photoFile.name
  if (photoPreviewFailed.value) return '原证件照暂时无法预览，请重新选择'
  if (model.photoUrl || model.storedPhotoUrl) return '已上传证件照'
  return '尚未选择照片'
})

watch(
  () => props.initialValue,
  (value) => {
    Object.assign(model, emptyApplication(), value || {})
    photoError.value = ''
    photoPreviewFailed.value = false
  },
  { immediate: true, deep: true },
)

function selectPhoto(event) {
  const file = event.target.files?.[0]
  if (!file) return
  if (!allowedPhotoTypes.has(file.type)) {
    photoError.value = '请选择 JPG、PNG 或 WebP 格式的图片'
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
  if (localPhotoUrl && typeof URL.revokeObjectURL === 'function') URL.revokeObjectURL(localPhotoUrl)
  if (typeof URL.createObjectURL === 'function') {
    localPhotoUrl = URL.createObjectURL(file)
    model.photoUrl = localPhotoUrl
  }
}

function openPhotoPicker() {
  photoInput.value?.click()
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
</script>

<template>
  <form class="application-form application-grid" @submit.prevent="emit('submit', snapshot())">
    <label>
      <span>姓名</span>
      <input v-model.trim="model.realName" name="realName" autocomplete="name" required />
    </label>
    <label>
      <span>性别</span>
      <select v-model="model.gender" name="gender" required>
        <option value="" disabled>请选择</option>
        <option value="男">男</option>
        <option value="女">女</option>
        <option value="未知">不便透露</option>
      </select>
    </label>
    <label class="application-grid__wide">
      <span>班级</span>
      <input v-model.trim="model.className" name="className" placeholder="例如：软工2402" required />
    </label>
    <label>
      <span>第一志愿</span>
      <select v-model="model.firstChoice" name="firstChoice" required>
        <option value="" disabled>请选择</option>
        <option v-for="department in recruitmentDepartments" :key="department.value" :value="department.value">
          {{ department.label }}
        </option>
      </select>
    </label>
    <label>
      <span>第二志愿</span>
      <select v-model="model.secondChoice" name="secondChoice" required>
        <option value="" disabled>请选择</option>
        <option v-for="department in recruitmentDepartments" :key="department.value" :value="department.value">
          {{ department.label }}
        </option>
      </select>
    </label>
    <div class="application-grid__wide application-form__photo-field">
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
            <span>JPG / PNG / WebP</span>
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
            accept="image/jpeg,image/png,image/webp"
            :required="!model.storedPhotoUrl && !model.photoUrl"
            @change="selectPhoto"
          />
          <button type="button" class="application-form__photo-button" @click="openPhotoPicker">
            <span aria-hidden="true">＋</span>{{ model.photoUrl ? '重新选择照片' : '选择照片' }}
          </button>
        </div>
      </div>
    </div>
    <label class="application-grid__wide">
      <span>自我介绍</span>
      <textarea v-model.trim="model.selfIntro" name="selfIntro" rows="5" required></textarea>
    </label>
    <fieldset class="application-grid__wide">
      <legend>是否有编程经验</legend>
      <label class="application-form__radio"><input v-model="model.codingExperience" type="radio" value="1" />有</label>
      <label class="application-form__radio"><input v-model="model.codingExperience" type="radio" value="0" />暂无</label>
    </fieldset>
    <label v-if="model.codingExperience === '1'" class="application-grid__wide">
      <span>编程经验</span>
      <textarea v-model.trim="model.codingExperienceDesc" name="codingExperienceDesc" rows="4"></textarea>
    </label>
    <label class="application-grid__wide">
      <span>你对 Quanta 的了解</span>
      <textarea v-model.trim="model.quantaUnderstanding" name="quantaUnderstanding" rows="4" required></textarea>
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
