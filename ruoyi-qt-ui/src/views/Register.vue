<template>
  <div class="auth-page">
    <section class="auth-card">
      <div class="brand">
        <div class="brand-logo">Q</div>
        <h1>新生注册</h1>
        <p>仅支持新生自助开户，塔员请联系管理员开通。</p>
      </div>

      <el-form ref="registerForm" :model="form" :rules="rules" class="login-form">
        <button class="back-link" type="button" @click="$router.push({ path: '/login', query: { role: 'freshman' } })">
          <i class="el-icon-arrow-left" /> 返回登录
        </button>
        <el-form-item prop="nickName">
          <el-input v-model.trim="form.nickName" placeholder="姓名/昵称" />
        </el-form-item>
        <el-form-item prop="studentNo">
          <el-input v-model.trim="form.studentNo" placeholder="学号" />
        </el-form-item>
        <el-form-item prop="className">
          <el-input v-model.trim="form.className" placeholder="班级，如 软工2402" />
        </el-form-item>
        <el-form-item prop="username">
          <el-input v-model.trim="form.username" placeholder="登录账号" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（5-20位）" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" @keyup.enter.native="handleRegister" />
        </el-form-item>
        <el-form-item v-if="captchaEnabled" prop="code">
          <div class="captcha-row">
            <el-input v-model.trim="form.code" placeholder="验证码" @keyup.enter.native="handleRegister" />
            <img :src="codeUrl" alt="验证码" @click="getCode" />
          </div>
        </el-form-item>
        <el-button class="login-button" type="primary" :loading="loading" @click="handleRegister">
          {{ loading ? '注册中...' : '注册并去登录' }}
        </el-button>
      </el-form>
    </section>
  </div>
</template>

<script>
import { getCodeImg, register } from '@/api/auth'

export default {
  name: 'Register',
  data() {
    const equalToPassword = (rule, value, callback) => {
      if (value !== this.form.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      codeUrl: '',
      captchaEnabled: true,
      loading: false,
      form: {
        nickName: '',
        studentNo: '',
        className: '',
        username: '',
        password: '',
        confirmPassword: '',
        code: '',
        uuid: '',
        loginType: '0'
      },
      rules: {
        nickName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
        className: [{ required: true, message: '请输入班级', trigger: 'blur' }],
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
          { min: 2, max: 20, message: '账号长度 2-20', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 5, max: 20, message: '密码长度 5-20', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: equalToPassword, trigger: 'blur' }
        ],
        code: [{ required: true, message: '请输入验证码', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getCode()
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = 'data:image/gif;base64,' + res.img
          this.form.uuid = res.uuid
        }
      })
    },
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (!valid) return
        this.loading = true
        const payload = {
          username: this.form.username,
          password: this.form.password,
          nickName: this.form.nickName,
          studentNo: this.form.studentNo,
          className: this.form.className,
          code: this.form.code,
          uuid: this.form.uuid,
          loginType: '0'
        }
        register(payload).then(() => {
          this.$message.success('注册成功，请使用新生入口登录')
          this.$router.push({ path: '/login', query: { role: 'freshman' } })
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
  padding: 56px 24px 32px;
  background:
    radial-gradient(circle at 24% 12%, rgba(253, 175, 50, 0.18), transparent 28%),
    linear-gradient(180deg, #fff 0%, #fafafa 58%, #f7f3ec 100%);
}

.brand {
  text-align: center;
  margin-bottom: 28px;
}

.brand-logo {
  width: 88px;
  height: 88px;
  margin: 0 auto;
  border-radius: 24px;
  background: #fdaf32;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 42px;
  font-weight: 900;
}

.brand h1 {
  margin: 18px 0 8px;
  font-size: 28px;
  font-weight: 900;
}

.brand p {
  margin: 0;
  color: #8c8c8c;
  line-height: 20px;
  padding: 0 12px;
}

.login-form {
  padding: 24px;
  background: rgba(255, 255, 255, 0.9);
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
  margin-top: 8px;
  border-radius: 23px;
  font-weight: 900;
}
</style>
