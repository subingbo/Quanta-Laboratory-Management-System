<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { getReservationRecords } from '@/api/workstations'
import './workstations.css'

const loading = ref(false)
const failed = ref(false)
const rows = ref([])

const statuses = {
  PENDING: { label: '待审核', type: 'warning' },
  APPROVED: { label: '已预约', type: 'warning' },
  CANCELED: { label: '已取消', type: 'info' },
  FINISHED: { label: '已完成', type: 'success' },
}

function parts(value = '') {
  const [date = '-', time = ''] = String(value).replace('T', ' ').split(' ')
  return { date, time: time.slice(0, 5) }
}

function dateLabel(row) {
  return parts(row.reserveStart).date
}

function timeLabel(row) {
  const start = parts(row.reserveStart)
  const end = parts(row.reserveEnd)
  return start.date === end.date
    ? `${start.time}-${end.time}`
    : `${start.time}-${end.date} ${end.time}`
}

function statusMeta(status) {
  return statuses[status] || { label: status || '-', type: 'info' }
}

async function loadList() {
  loading.value = true
  failed.value = false
  try {
    const result = await getReservationRecords()
    rows.value = result.rows
  } catch (error) {
    failed.value = true
    ElMessage.error(error.message || '工位预约记录加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadList)
</script>

<template>
  <section class="page-card workstations-card">
    <header class="workstations-card__header"><h2>工位预约记录</h2></header>

    <div v-if="failed && !loading" class="workstations-card__error">
      <span>预约记录暂时无法加载。</span>
      <ElButton type="primary" link @click="loadList">重新加载</ElButton>
    </div>

    <ElTable
      v-else
      v-loading="loading"
      class="workstations-table"
      :data="rows"
      size="small"
      row-key="reservationId"
      empty-text="暂无预约记录"
    >
      <ElTableColumn label="预约人" min-width="180">
        <template #default="{ row }">{{ row.nickName || row.userName || '-' }}</template>
      </ElTableColumn>
      <ElTableColumn label="工位号" min-width="180">
        <template #default="{ row }"><span class="workstations-table__code">{{ row.workstationCode || '-' }}</span></template>
      </ElTableColumn>
      <ElTableColumn label="日期" min-width="250">
        <template #default="{ row }">{{ dateLabel(row) }}</template>
      </ElTableColumn>
      <ElTableColumn label="时间段" min-width="280">
        <template #default="{ row }">{{ timeLabel(row) }}</template>
      </ElTableColumn>
      <ElTableColumn label="状态" min-width="190">
        <template #default="{ row }">
          <StatusTag :type="statusMeta(row.status).type">{{ statusMeta(row.status).label }}</StatusTag>
        </template>
      </ElTableColumn>
    </ElTable>
  </section>
</template>
