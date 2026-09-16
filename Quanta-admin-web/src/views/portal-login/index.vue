<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { homePathFor } from '@/utils/session-audience'
import { safeInternalRedirect } from '@/utils/redirect'
import quantaLogoUrl from '@/assets/quanta-logo.jpg'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const captchaEnabled = ref(false)
const captchaImage = ref('')
const audience = computed(() => (route.meta.portalAudience === 'freshman' ? 'freshman' : 'member'))
const audienceLabel = computed(() => (audience.value === 'freshman' ? '新生' : '塔员'))
const form = reactive({ username: '', password: '', code: '', uuid: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  code: [{
    validator: (_, value, callback) => {
      if (captchaEnabled.value && !value) callback(new Error('请输入验证码'))
      else callback()
    },
    trigger: 'blur',
  }],
}

async function loadCaptcha() {
  try {
    const response = await getCaptcha()
    captchaEnabled.value = response.captchaEnabled !== false
    captchaImage.value = response.img ? `data:image/gif;base64,${response.img}` : ''
    form.uuid = response.uuid || ''
  } catch {
    captchaEnabled.value = false
  }
}

async function submit() {
  if (loading.value) return
  const valid = await formRef.value?.validate().catch(() => false)
  if (valid !== true) return
  loading.value = true
  try {
    await userStore.login({ ...form }, audience.value)
    await userStore.fetchUserInfo()
    const intended = safeInternalRedirect(route.query.redirect, homePathFor(audience.value))
    await router.replace(intended)
    ElMessage.success('登录成功')
  } catch (error) {
    userStore.reset()
    form.password = ''
    form.code = ''
    ElMessage.error(error.message || '登录失败')
    if (captchaEnabled.value) await loadCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(loadCaptcha)
</script>

<template>
  <main class="portal-login">
    <RouterLink class="portal-login__back" to="/">← 返回入口</RouterLink>
    <section class="portal-login__card">
      <img :src="quantaLogoUrl" alt="Quanta 社团 Logo" />
      <p>QUANTA DIGITAL CLUB</p>
      <h1>{{ audienceLabel }}登录</h1>
      <ElForm ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="submit">
        <ElFormItem prop="username">
          <ElInput v-model.trim="form.username" autocomplete="username" placeholder="用户名" />
        </ElFormItem>
        <ElFormItem prop="password">
          <ElInput v-model="form.password" autocomplete="current-password" type="password" show-password placeholder="密码" @keyup.enter="submit" />
        </ElFormItem>
        <ElFormItem v-if="captchaEnabled" prop="code">
          <div class="portal-login__captcha">
            <ElInput v-model="form.code" placeholder="验证码" @keyup.enter="submit" />
            <button type="button" aria-label="刷新验证码" @click="loadCaptcha">
              <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
              <span v-else>刷新</span>
            </button>
          </div>
        </ElFormItem>
        <ElButton type="primary" native-type="button" :loading="loading" @click="submit">登录</ElButton>
      </ElForm>
    </section>
  </main>
</template>

<style scoped src="./portal-login.css"></style>
