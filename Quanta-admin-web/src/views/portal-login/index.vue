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
const audienceCopy = computed(() => audience.value === 'freshman'
  ? '了解 Quanta，提交你的招新报名，随时查看面试进展。'
  : '连接成员与社团服务，让每一次协作都有清晰入口。')
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
    <section class="portal-login__brand-panel">
      <RouterLink class="portal-login__brand" to="/">
        <img :src="quantaLogoUrl" alt="Quanta 社团 Logo" />
        <span><strong>Quanta</strong><small>DIGITAL CLUB</small></span>
      </RouterLink>
      <div class="portal-login__brand-copy">
        <p><i></i> {{ audience === 'freshman' ? 'JOIN QUANTA' : 'MEMBER SPACE' }}</p>
        <h1>{{ audience === 'freshman' ? '从这里，开始你的 Quanta 旅程。' : '欢迎回来，继续一起创造。' }}</h1>
        <span>{{ audienceCopy }}</span>
      </div>
      <div class="portal-login__brand-index" aria-hidden="true">{{ audience === 'freshman' ? '01' : '02' }}</div>
    </section>
    <section class="portal-login__form-panel">
      <RouterLink class="portal-login__back" to="/">← 返回身份选择</RouterLink>
      <div class="portal-login__card">
        <span class="portal-login__identity">{{ audienceLabel }} PORTAL</span>
        <h2>{{ audienceLabel }}登录</h2>
        <p class="portal-login__welcome">使用你的 Quanta 账号继续访问</p>
        <ElForm ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="submit">
          <ElFormItem prop="username" label="用户名">
            <ElInput v-model.trim="form.username" autocomplete="username" placeholder="请输入用户名" />
          </ElFormItem>
          <ElFormItem prop="password" label="密码">
            <ElInput v-model="form.password" autocomplete="current-password" type="password" show-password placeholder="请输入密码" @keyup.enter="submit" />
          </ElFormItem>
          <ElFormItem v-if="captchaEnabled" prop="code" label="验证码">
            <div class="portal-login__captcha">
              <ElInput v-model="form.code" placeholder="请输入验证码" @keyup.enter="submit" />
              <button type="button" aria-label="刷新验证码" @click="loadCaptcha">
                <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
                <span v-else>刷新</span>
              </button>
            </div>
          </ElFormItem>
          <ElButton class="portal-login__submit" type="primary" native-type="button" :loading="loading" @click="submit">
            登录并进入{{ audienceLabel }}门户
          </ElButton>
        </ElForm>
        <p class="portal-login__hint">登录即代表你正在访问 Quanta 社团统一服务门户</p>
      </div>
    </section>
  </main>
</template>

<style scoped src="./portal-login.css"></style>
