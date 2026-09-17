<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { changePassword, updateFreshmanEmail } from '@/api/portal/account'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
const isFreshman = computed(() => route.meta.portalAudience === 'freshman')
const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const emailForm = reactive({ email: '' })
const submitting = ref(false)
const emailSubmitting = ref(false)
const errorMessage = ref('')
const emailError = ref('')
const currentEmail = computed(() => userStore.user?.email || '')
const emailPattern = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/

watch(currentEmail, (value) => { emailForm.email = value }, { immediate: true })

function validate() {
  if (!form.oldPassword || !form.newPassword || !form.confirmPassword) return '请完整填写密码信息'
  if (form.newPassword.length < 6) return '新密码至少需要 6 位'
  if (form.newPassword !== form.confirmPassword) return '两次输入的新密码不一致'
  if (form.oldPassword === form.newPassword) return '新密码不能与旧密码相同'
  return ''
}

async function submit() {
  if (submitting.value) return
  errorMessage.value = validate()
  if (errorMessage.value) return
  submitting.value = true
  try {
    await changePassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
    form.oldPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
    ElMessage.success('密码修改成功')
  } catch (error) {
    errorMessage.value = error.message || '密码修改失败'
    ElMessage.error(errorMessage.value)
  } finally {
    submitting.value = false
  }
}

async function submitEmail() {
  if (emailSubmitting.value) return
  const email = emailForm.email.trim()
  if (!email) emailError.value = '请输入邮箱'
  else if (email.length > 50 || !emailPattern.test(email)) emailError.value = '请输入正确的邮箱'
  else if (email === currentEmail.value) emailError.value = '新邮箱与当前邮箱相同'
  else emailError.value = ''
  if (emailError.value) return

  emailSubmitting.value = true
  try {
    await updateFreshmanEmail(email)
    await userStore.fetchUserInfo()
    ElMessage.success('邮箱修改成功')
  } catch (error) {
    emailError.value = error.message || '邮箱修改失败'
    ElMessage.error(emailError.value)
  } finally {
    emailSubmitting.value = false
  }
}
</script>

<template>
  <section class="portal-security">
    <header>
      <p>ACCOUNT SECURITY</p>
      <h1>账号与安全</h1>
      <span>{{ isFreshman ? '维护你的登录密码与联系邮箱，确保招新通知能够及时送达。' : '定期更新密码，并避免在多个平台使用相同密码。' }}</span>
    </header>
    <div class="portal-security__grid" :class="{ 'has-email-card': isFreshman }">
      <form class="portal-security__card portal-card" @submit.prevent="submit">
        <div class="portal-security__intro">
          <span aria-hidden="true">•••</span>
          <div><h2>修改登录密码</h2><p>修改后，下次登录请使用新密码。</p></div>
        </div>
        <p v-if="errorMessage" class="portal-security__error" role="alert">{{ errorMessage }}</p>
        <label>当前密码<input v-model="form.oldPassword" name="oldPassword" type="password" autocomplete="current-password" required /></label>
        <label>新密码<input v-model="form.newPassword" name="newPassword" type="password" autocomplete="new-password" minlength="6" required /></label>
        <label>确认新密码<input v-model="form.confirmPassword" name="confirmPassword" type="password" autocomplete="new-password" minlength="6" required /></label>
        <button class="portal-security__submit" type="submit" :disabled="submitting">
          {{ submitting ? '提交中…' : '确认修改' }}
        </button>
      </form>

      <form v-if="isFreshman" class="portal-security__card portal-card" data-testid="email-card" @submit.prevent="submitEmail">
        <div class="portal-security__intro portal-security__intro--email">
          <span aria-hidden="true">@</span>
          <div><h2>修改联系邮箱</h2><p>面试安排和重要通知将发送到此邮箱。</p></div>
        </div>
        <div class="portal-security__current-email">
          <span>当前邮箱</span>
          <strong>{{ currentEmail || '暂未设置' }}</strong>
        </div>
        <p v-if="emailError" class="portal-security__error" role="alert">{{ emailError }}</p>
        <label>新邮箱<input v-model.trim="emailForm.email" name="email" type="email" autocomplete="email" maxlength="50" placeholder="name@example.com" required /></label>
        <button data-testid="email-submit" class="portal-security__submit" type="submit" :disabled="emailSubmitting">
          {{ emailSubmitting ? '提交中…' : '保存邮箱' }}
        </button>
      </form>
    </div>
  </section>
</template>

<style scoped src="./security.css"></style>
