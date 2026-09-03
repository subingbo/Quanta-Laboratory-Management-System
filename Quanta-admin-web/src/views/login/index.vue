<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { getCaptcha } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { safeInternalRedirect } from '@/utils/redirect'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const captchaEnabled = ref(false)
const captchaImage = ref('')
const isDev = import.meta.env.DEV

const form = reactive({
  username: '',
  password: '',
  code: '',
  uuid: '',
  loginType: '1',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  code: [
    {
      validator: (_, value, callback) => {
        if (captchaEnabled.value && !value) callback(new Error('请输入验证码'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
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
  if (!form.username.trim() || !form.password) {
    await formRef.value?.validate().catch(() => false)
    return
  }
  const valid = await formRef.value?.validate().catch(() => false)
  if (valid !== true) return

  loading.value = true
  try {
    await userStore.login({ ...form })
    ElMessage.success('登录成功')
    await router.replace(safeInternalRedirect(route.query.redirect))
  } catch (error) {
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
  <main class="login-page">
    <section class="login-page__brand-panel">
      <div class="login-page__brand">
        <div class="login-page__logo">Q</div>
        <div>
          <strong>Quanta</strong>
          <span>在热爱中创造，在协作中成长</span>
        </div>
      </div>
      <div class="login-page__intro">
        <span>QUANTA ADMIN</span>
        <h1>让社团管理<br />清晰而高效</h1>
        <p>成员、招新与日常事务，在一个统一、安全的后台中协同完成。</p>
      </div>
      <div class="login-page__decoration">Q</div>
    </section>

    <section class="login-page__form-panel">
      <div class="login-card">
        <div class="login-card__mobile-brand">
          <div class="login-page__logo">Q</div>
          <strong>Quanta</strong>
        </div>
        <div class="login-card__heading">
          <span>欢迎回来</span>
          <h2>登录后台管理系统</h2>
          <p>请输入你的账号信息继续访问</p>
        </div>

        <ElForm ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="submit">
          <ElFormItem prop="username">
            <ElInput v-model="form.username" :prefix-icon="User" placeholder="用户名" autocomplete="username" />
          </ElFormItem>
          <ElFormItem prop="password">
            <ElInput
              v-model="form.password"
              :prefix-icon="Lock"
              placeholder="密码"
              type="password"
              autocomplete="current-password"
              show-password
              @keyup.enter="submit"
            />
          </ElFormItem>
          <ElFormItem v-if="captchaEnabled" prop="code">
            <div class="login-card__captcha">
              <ElInput v-model="form.code" placeholder="验证码" @keyup.enter="submit" />
              <button type="button" aria-label="刷新验证码" @click="loadCaptcha">
                <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
                <span v-else>刷新</span>
              </button>
            </div>
          </ElFormItem>
          <ElButton
            class="login-card__submit quanta-primary-button"
            type="primary"
            native-type="button"
            :loading="loading"
            @click="submit"
          >
            登录
          </ElButton>
        </ElForm>

        <div v-if="isDev" class="login-card__demo">
          <span>本地开发账号(密码 admin123)</span>
          <code>admin(塔员/CEO)</code>
          <code>qt_member(塔员)</code>
        </div>
      </div>
      <p class="login-page__copyright">© 2026 Quanta · 后台管理系统</p>
    </section>
  </main>
</template>

<style scoped src="./login.css"></style>
