<template>
  <mobile-shell title="Join us" back hide-nav>
    <section class="join-hero">
      <h2>成为 Quanta 的一部分</h2>
      <p>选择两个志愿，上传照片，提交你的自我介绍。</p>
    </section>

    <el-form class="card apply-form" :model="form">
      <el-input v-model.trim="form.realName" placeholder="真实姓名" />
      <el-input v-model.trim="form.className" placeholder="班级" />
      <el-select v-model="form.gender" placeholder="性别">
        <el-option label="男" value="0" />
        <el-option label="女" value="1" />
      </el-select>
      <el-select v-model="form.firstChoice" placeholder="第一志愿">
        <el-option v-for="dept in departments" :key="dept.value" :label="dept.label" :value="dept.value" />
      </el-select>
      <el-select v-model="form.secondChoice" placeholder="第二志愿">
        <el-option v-for="dept in departments" :key="dept.value" :label="dept.label" :value="dept.value" />
      </el-select>
      <el-input v-model.trim="form.selfIntro" type="textarea" :rows="4" placeholder="自我介绍" />
      <el-input v-model.trim="form.codingExperienceDesc" type="textarea" :rows="3" placeholder="技术经历（没有也可以写学习兴趣）" />
      <el-input v-model.trim="form.quantaUnderstanding" type="textarea" :rows="3" placeholder="你对 Quanta 的理解" />
      <label class="upload-card">
        <i class="el-icon-camera" />
        <span>{{ photoLabel }}</span>
        <input type="file" accept="image/*" @change="onPhotoChange" />
      </label>
      <button class="primary-pill block-btn" type="button" :disabled="submitting" @click="submitApply">
        {{ submitting ? '提交中...' : '投递简历' }}
      </button>
    </el-form>
  </mobile-shell>
</template>

<script>
import MobileShell from '@/components/MobileShell.vue'
import { applyInterview, getMyInterviewApplication } from '@/api/qt'
import { deptLabel } from '@/utils/helpers'

const SUBMIT_KEYS = [
  'realName',
  'gender',
  'className',
  'firstChoice',
  'secondChoice',
  'selfIntro',
  'codingExperience',
  'codingExperienceDesc',
  'quantaUnderstanding',
  'photoUrl'
]

export default {
  name: 'Join',
  components: { MobileShell },
  data() {
    return {
      submitting: false,
      photoFile: null,
      hasExistingPhoto: false,
      departments: [
        { label: '后端', value: 'BACKEND' },
        { label: '产品', value: 'PRODUCT' },
        { label: '设计', value: 'DESIGN' },
        { label: '前端', value: 'FRONTEND' },
        { label: '安卓', value: 'ANDROID' }
      ],
      form: {
        realName: '',
        gender: '',
        className: '',
        firstChoice: '',
        secondChoice: '',
        selfIntro: '',
        codingExperience: '0',
        codingExperienceDesc: '',
        quantaUnderstanding: '',
        photoUrl: ''
      }
    }
  },
  computed: {
    photoLabel() {
      if (this.photoFile) {
        return this.photoFile.name
      }
      return this.hasExistingPhoto ? '已上传证件照（可重新选择）' : '上传证件照'
    }
  },
  created() {
    getMyInterviewApplication().then(res => {
      const data = res.data || {}
      const application = data.application || {}
      const profile = data.profile || {}
      SUBMIT_KEYS.forEach(key => {
        if (application[key] !== undefined && application[key] !== null && application[key] !== '') {
          this.form[key] = application[key]
        }
        if (profile[key] !== undefined && profile[key] !== null && profile[key] !== '') {
          this.form[key] = profile[key]
        }
      })
      this.hasExistingPhoto = !!application.photoUrl
    }).catch(() => {})
  },
  methods: {
    onPhotoChange(event) {
      this.photoFile = event.target.files && event.target.files[0]
    },
    submitApply() {
      if (!this.form.firstChoice || !this.form.secondChoice) {
        this.$message.warning('请选择两个志愿')
        return
      }
      if (this.form.firstChoice === this.form.secondChoice) {
        this.$message.warning('两个志愿不能相同')
        return
      }
      if (!this.photoFile && !this.form.photoUrl) {
        this.$message.warning('请上传证件照')
        return
      }
      const data = new FormData()
      SUBMIT_KEYS.forEach(key => {
        const value = this.form[key]
        if (value !== undefined && value !== null && value !== '') {
          data.append(key, value)
        }
      })
      if (this.photoFile) {
        data.append('photoFile', this.photoFile)
      }
      this.submitting = true
      applyInterview(data).then(() => {
        this.$message.success('投递成功')
        this.$router.push('/interview/results')
      }).finally(() => {
        this.submitting = false
      })
    },
    deptLabel
  }
}
</script>

<style lang="scss" scoped>
.join-hero {
  border-radius: 24px;
  background: #282828;
  color: #fff;
  padding: 24px;
  margin-bottom: 16px;
}

.join-hero h2 {
  margin: 0 0 10px;
  font-size: 28px;
  font-weight: 900;
}

.join-hero p {
  margin: 0;
  color: rgba(255, 255, 255, 0.7);
  line-height: 22px;
}

.apply-form {
  padding: 18px;
}

.apply-form .el-input,
.apply-form .el-select,
.apply-form .el-textarea {
  width: 100%;
  margin-bottom: 12px;
}

.upload-card {
  height: 72px;
  border-radius: 18px;
  border: 1px dashed #fdaf32;
  background: #fff6ea;
  color: #fdaf32;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 900;
  margin-bottom: 18px;
  position: relative;
}

.upload-card input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.block-btn {
  width: 100%;
}
</style>
