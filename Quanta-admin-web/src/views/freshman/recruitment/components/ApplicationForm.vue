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
import { reactive, watch } from 'vue'
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

watch(
  () => props.initialValue,
  (value) => Object.assign(model, emptyApplication(), value || {}),
  { immediate: true, deep: true },
)

function selectPhoto(event) {
  const file = event.target.files?.[0]
  if (!file) return
  model.photoFile = file
  if (typeof URL.createObjectURL === 'function') model.photoUrl = URL.createObjectURL(file)
}

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
    <label class="application-grid__wide">
      <span>证件照</span>
      <input
        name="photoFile"
        type="file"
        accept="image/jpeg,image/png,image/webp"
        :required="!model.storedPhotoUrl && !model.photoUrl"
        @change="selectPhoto"
      />
      <small>支持 JPG、PNG 或 WebP，服务端上限 5 MB</small>
      <img v-if="model.photoUrl" class="application-form__photo" :src="model.photoUrl" alt="当前证件照预览" />
    </label>
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
