<template>
  <mobile-shell title="账号安全" back hide-nav>
    <el-form ref="profileForm" :model="profile" class="card form-card">
      <h2>个人资料</h2>
      <el-form-item label="昵称">
        <el-input v-model.trim="profile.nickName" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model.trim="profile.phonenumber" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model.trim="profile.email" />
      </el-form-item>
      <button class="primary-pill block-btn" type="button" @click="saveProfile">保存资料</button>
    </el-form>

    <el-form ref="pwdForm" :model="passwordForm" class="card form-card">
      <h2>修改密码</h2>
      <el-form-item label="旧密码">
        <el-input v-model="passwordForm.oldPassword" type="password" />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="passwordForm.newPassword" type="password" />
      </el-form-item>
      <button class="primary-pill block-btn" type="button" @click="savePassword">修改密码</button>
    </el-form>
  </mobile-shell>
</template>

<script>
import MobileShell from '@/components/MobileShell.vue'
import { getProfile, updatePassword, updateProfile } from '@/api/qt'
import { setUser } from '@/utils/auth'

export default {
  name: 'AccountSecurity',
  components: { MobileShell },
  data() {
    return {
      profile: {},
      passwordForm: {
        oldPassword: '',
        newPassword: ''
      }
    }
  },
  created() {
    getProfile().then(res => {
      this.profile = res.data || {}
    })
  },
  methods: {
    saveProfile() {
      updateProfile(this.profile).then(() => {
        setUser(this.profile)
        this.$message.success('资料已保存')
      })
    },
    savePassword() {
      if (!this.passwordForm.oldPassword || !this.passwordForm.newPassword) {
        this.$message.warning('请输入完整密码')
        return
      }
      updatePassword(this.passwordForm).then(() => {
        this.$message.success('密码已修改')
        this.passwordForm.oldPassword = ''
        this.passwordForm.newPassword = ''
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.form-card {
  padding: 20px;
  margin-bottom: 16px;
}

.form-card h2 {
  margin: 0 0 18px;
  font-size: 22px;
  font-weight: 900;
}

.block-btn {
  width: 100%;
}
</style>
