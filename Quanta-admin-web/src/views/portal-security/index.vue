<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword } from '@/api/portal/account'

const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const submitting = ref(false)
const errorMessage = ref('')

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
</script>

<template>
  <section class="portal-security">
    <header>
      <p>ACCOUNT SECURITY</p>
      <h1>账号与安全</h1>
      <span>定期更新密码，并避免在多个平台使用相同密码。</span>
    </header>
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
  </section>
</template>

<style scoped src="./security.css"></style>
