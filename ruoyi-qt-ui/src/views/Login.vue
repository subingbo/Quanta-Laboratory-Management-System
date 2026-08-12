<template>
  <div class="auth-page">
    <section class="auth-card">
      <div class="brand">
        <div class="brand-logo">Q</div>
        <h1>Quanta</h1>
        <p>Nothing but professional.</p>
      </div>

      <div v-if="!selectedRole" class="role-panel">
        <h2>选择身份</h2>
        <button type="button" class="role-card" @click="selectRole('freshman')">
          <span class="role-icon"><i class="el-icon-school" /></span>
          <span>
            <strong>新生入口</strong>
            <small>报名、查看面试进度与活动信息</small>
          </span>
          <i class="el-icon-arrow-right" />
        </button>
        <button type="button" class="role-card" @click="selectRole('member')">
          <span class="role-icon"><i class="el-icon-user-solid" /></span>
          <span>
            <strong>塔员入口</strong>
            <small>预约工位、借阅图书、订购塔服</small>
          </span>
          <i class="el-icon-arrow-right" />
        </button>
      </div>

      <el-form v-else ref="loginForm" :model="loginForm" :rules="rules" class="login-form">
        <button class="back-link" type="button" @click="selectedRole = ''">
          <i class="el-icon-arrow-left" /> 切换身份
        </button>
        <h2>{{ selectedRole === 'member' ? '塔员' : '新生' }}登录</h2>
        <el-form-item prop="username">
          <el-input v-model.trim="loginForm.username" placeholder="账号" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" @keyup.enter.native="handleLogin" />
        </el-form-item>
        <el-form-item v-if="captchaEnabled" prop="code">
          <div class="captcha-row">
            <el-input v-model.trim="loginForm.code" placeholder="验证码" @keyup.enter.native="handleLogin" />
            <img :src="codeUrl" alt="验证码" @click="getCode" />
          </div>
        </el-form-item>
        <el-checkbox v-model="rememberMe">记住登录</el-checkbox>
        <el-button class="login-button" type="primary" :loading="loading" @click="handleLogin">
          {{ loading ? '登录中...' : '登录' }}
        </el-button>
        <button
          v-if="selectedRole === 'freshman'"
          class="register-link"
          type="button"
          @click="$router.push('/register')"
        >
          没有账号？新生注册
        </button>
      </el-form>
    </section>
  </div>
</template>

<script>
import { getCodeImg, getInfo, login } from '@/api/auth'
import { setToken, setUser } from '@/utils/auth'

export default {
  name: 'Login',
  data() {
    return {
      selectedRole: this.$route.query.role || '',
      redirect: this.$route.query.redirect || '/home',
      codeUrl: '',
      captchaEnabled: true,
      rememberMe: false,
      loading: false,
      loginForm: {
        username: '',
        password: '',
        code: '',
        uuid: ''
      },
      rules: {
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        code: [{ required: true, message: '请输入验证码', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getCode()
  },
  methods: {
    selectRole(role) {
      this.selectedRole = role
      this.$router.replace({ path: '/login', query: { role, redirect: this.redirect } })
    },
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = 'data:image/gif;base64,' + res.img
          this.loginForm.uuid = res.uuid
        }
      })
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (!valid) return
        this.loading = true
        const payload = Object.assign({}, this.loginForm, {
          loginType: this.selectedRole === 'member' ? '1' : '0'
        })
        login(payload).then(res => {
          setToken(res.token)
          return getInfo()
        }).then(res => {
          setUser(res.user)
          this.$router.push(this.redirect || '/home')
        }).catch(() => {
          if (this.captchaEnabled) this.getCode()
        }).finally(() => {
          this.loading = false
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  background: #fafafa;
}

.auth-card {
  width: 100%;
  max-width: 430px;
  min-height: 100vh;
  padding: 76px 24px 32px;
  background:
    radial-gradient(circle at 24% 12%, rgba(253, 175, 50, 0.18), transparent 28%),
    linear-gradient(180deg, #fff 0%, #fafafa 58%, #f7f3ec 100%);
}

.brand {
  text-align: center;
  margin-bottom: 44px;
}

.brand-logo {
  width: 112px;
  height: 112px;
  margin: 0 auto;
  border-radius: 28px;
  background: #fdaf32;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 54px;
  font-weight: 900;
  box-shadow: 0 18px 42px rgba(253, 175, 50, 0.22);
}

.brand h1 {
  margin: 22px 0 6px;
  font-size: 34px;
  line-height: 42px;
  font-weight: 900;
}

.brand p {
  margin: 0;
  color: #8c8c8c;
}

.role-panel h2,
.login-form h2 {
  margin: 0 0 22px;
  font-size: 24px;
  font-weight: 900;
}

.role-card {
  width: 100%;
  min-height: 92px;
  margin-bottom: 16px;
  padding: 18px;
  border: 1px solid #f0f0f0;
  border-radius: 22px;
  background: #fff;
  display: grid;
  grid-template-columns: 48px 1fr 20px;
  align-items: center;
  gap: 14px;
  text-align: left;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.role-card strong {
  display: block;
  font-size: 17px;
  line-height: 24px;
}

.role-card small {
  display: block;
  color: #888;
  line-height: 18px;
  margin-top: 3px;
}

.role-icon {
  width: 48px;
  height: 48px;
  border-radius: 24px;
  background: #ffedd4;
  color: #fdaf32;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.login-form {
  padding: 24px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 26px;
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
}

.back-link {
  border: none;
  background: transparent;
  padding: 0;
  margin: 0 0 18px;
  color: #8c8c8c;
}

.captcha-row {
  display: grid;
  grid-template-columns: 1fr 112px;
  gap: 10px;
}

.captcha-row img {
  width: 112px;
  height: 40px;
  border-radius: 12px;
  object-fit: cover;
}

.login-button {
  width: 100%;
  height: 46px;
  margin-top: 20px;
  border-radius: 23px;
  font-weight: 900;
  box-shadow: 0 12px 24px rgba(253, 175, 50, 0.28);
}

.register-link {
  display: block;
  width: 100%;
  margin-top: 16px;
  border: none;
  background: transparent;
  color: #fdaf32;
  font-weight: 800;
}
</style>
