<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createReservation, getWorkstations } from '@/api/portal/workstations'

function today() {
  const value = new Date()
  const pad = (number) => String(number).padStart(2, '0')
  return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())}`
}

const workstations = ref([])
const loading = ref(true)
const errorMessage = ref('')
const submittingId = ref(null)
const form = reactive({ reserveDate: today(), startTime: '09:00', endTime: '12:00', purpose: '' })

async function loadWorkstations() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await getWorkstations({ status: '0' })
    workstations.value = result.rows
  } catch (error) {
    errorMessage.value = error.message || '工位加载失败'
  } finally {
    loading.value = false
  }
}

async function reserve(workstation) {
  if (submittingId.value || !form.reserveDate || !form.startTime || !form.endTime) return
  submittingId.value = workstation.workstationId
  try {
    await createReservation({
      workstationId: workstation.workstationId,
      reserveStart: `${form.reserveDate}T${form.startTime}:00`,
      reserveEnd: `${form.reserveDate}T${form.endTime}:00`,
      purpose: form.purpose.trim(),
    })
    ElMessage.success('工位预约成功')
    await loadWorkstations()
  } catch (error) {
    ElMessage.error(error.message || '工位预约失败')
  } finally {
    submittingId.value = null
  }
}

onMounted(loadWorkstations)
</script>

<template>
  <section class="member-service-page">
    <header class="member-service-heading"><div><p>WORKSTATIONS</p><h1>工位预约</h1><span>选择完整日期与时间段，预约适合你的学习工位。</span></div></header>
    <div class="member-reservation-form portal-card">
      <label>预约日期<input v-model="form.reserveDate" name="reserveDate" type="date" required /></label>
      <label>开始时间<input v-model="form.startTime" name="reserveStartTime" type="time" required /></label>
      <label>结束时间<input v-model="form.endTime" name="reserveEndTime" type="time" required /></label>
      <label class="is-wide">用途（选填）<input v-model="form.purpose" type="text" maxlength="100" placeholder="例如：项目讨论 / 自习" /></label>
    </div>
    <p v-if="errorMessage" class="member-feedback is-error" role="alert">{{ errorMessage }} <button type="button" @click="loadWorkstations">重试</button></p>
    <div v-if="loading" class="member-service-state portal-card">正在加载可用工位…</div>
    <div v-else-if="workstations.length" class="member-workstation-grid">
      <article v-for="station in workstations" :key="station.workstationId" class="member-workstation-card portal-card">
        <div class="member-workstation-card__mark" aria-hidden="true">{{ station.workstationCode?.slice(-2) || station.workstationId }}</div>
        <div><span>{{ station.locationDesc || '位置待确认' }}</span><h2>{{ station.workstationCode || `工位 #${station.workstationId}` }}</h2><p>{{ station.available ? '当前可预约' : '暂不可预约' }}</p></div>
        <button :data-testid="`reserve-button-${station.workstationId}`" class="member-primary-button" type="button" :disabled="!station.available || Boolean(submittingId)" @click="reserve(station)">{{ submittingId === station.workstationId ? '预约中…' : '预约此工位' }}</button>
      </article>
    </div>
    <div v-else class="member-service-state portal-card"><strong>暂无可预约工位</strong><p>稍后刷新页面，或选择其他日期再来看看。</p></div>
  </section>
</template>

<style src="../services/services.css"></style>
