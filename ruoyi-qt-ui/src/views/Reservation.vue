<template>
  <mobile-shell title="工位预约" back hide-nav>
    <section class="form-panel">
      <h2>预约时间</h2>
      <el-date-picker v-model="form.reserveStart" value-format="yyyy-MM-dd" type="date" placeholder="开始日期" />
      <el-date-picker v-model="form.reserveEnd" value-format="yyyy-MM-dd" type="date" placeholder="结束日期" />
      <el-input v-model.trim="form.purpose" type="textarea" :rows="2" placeholder="用途，例如：项目开发、学习、面试准备" />
    </section>

    <section class="section-title">
      <h3>选择工位</h3>
      <button type="button" @click="fetchWorkstations">刷新</button>
    </section>

    <div v-loading="loading" class="station-grid">
      <button
        v-for="station in workstations"
        :key="station.workstationId"
        type="button"
        class="station-slot"
        :class="{ active: form.workstationId === station.workstationId }"
        @click="form.workstationId = station.workstationId"
      >
        <strong>{{ station.workstationCode }}</strong>
        <span>{{ station.locationDesc || 'Quanta Lab' }}</span>
      </button>
      <div v-if="!loading && !workstations.length" class="empty-state">
        <i class="el-icon-office-building" />
        <span>暂无可预约工位</span>
      </div>
    </div>

    <button class="fixed-action" type="button" :disabled="!canSubmit || submitting" @click="submitReservation">
      {{ submitting ? '提交中...' : '提交预约' }}
    </button>
  </mobile-shell>
</template>

<script>
import MobileShell from '@/components/MobileShell.vue'
import { listWorkstations, reserveWorkstation } from '@/api/qt'
import { getUser } from '@/utils/auth'
import { plusDays, rowsOf, today } from '@/utils/helpers'

export default {
  name: 'Reservation',
  components: { MobileShell },
  data() {
    return {
      loading: false,
      submitting: false,
      workstations: [],
      form: {
        workstationId: null,
        reserveStart: today(),
        reserveEnd: plusDays(1),
        purpose: ''
      }
    }
  },
  computed: {
    canSubmit() {
      return this.form.workstationId && this.form.reserveStart && this.form.reserveEnd
    }
  },
  created() {
    this.fetchWorkstations()
  },
  methods: {
    fetchWorkstations() {
      this.loading = true
      listWorkstations({ pageNum: 1, pageSize: 100, status: '0' }).then(res => {
        this.workstations = rowsOf(res)
      }).finally(() => {
        this.loading = false
      })
    },
    submitReservation() {
      const user = getUser() || {}
      this.submitting = true
      reserveWorkstation({
        ...this.form,
        userId: user.userId,
        status: 'PENDING'
      }).then(() => {
        this.$message.success('预约已提交')
        this.$router.push('/services/my')
      }).finally(() => {
        this.submitting = false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.form-panel {
  border-radius: 24px;
  background: #fff;
  padding: 20px;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.05);
}

.form-panel h2 {
  margin: 0 0 16px;
  font-size: 22px;
  font-weight: 900;
}

.el-date-editor.el-input,
.el-textarea {
  width: 100%;
  margin-bottom: 12px;
}

.station-grid {
  min-height: 420px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.station-slot {
  min-height: 76px;
  border: 1px solid #f0f0f0;
  border-radius: 18px;
  background: #fff;
  color: #333;
  padding: 10px;
  text-align: center;
}

.station-slot.active {
  border-color: #fdaf32;
  background: #fff6ea;
  box-shadow: 0 10px 18px rgba(253, 175, 50, 0.18);
}

.station-slot strong {
  display: block;
  font-size: 18px;
  line-height: 24px;
  font-weight: 900;
}

.station-slot span {
  display: block;
  margin-top: 4px;
  color: #888;
  font-size: 11px;
  line-height: 15px;
}

.fixed-action {
  position: sticky;
  bottom: 0;
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 24px;
  background: #fdaf32;
  color: #fff;
  font-size: 16px;
  font-weight: 900;
  box-shadow: 0 12px 24px rgba(253, 175, 50, 0.25);
}

.fixed-action:disabled {
  background: #ddd;
  box-shadow: none;
}
</style>
