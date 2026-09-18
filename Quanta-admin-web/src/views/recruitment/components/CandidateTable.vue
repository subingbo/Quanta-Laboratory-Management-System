<script setup>
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import PermissionButton from '@/components/PermissionButton.vue'
import StatusTag from '@/components/StatusTag.vue'
import { departmentLabels, resultLabels } from '@/api/recruitment'
import { useUserStore } from '@/stores/user'
import { isCeoRole } from '@/composables/usePermission'

const props = defineProps({
  rows: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  roundId: { type: Number, default: 1 },
  pendingAction: { type: String, default: '' },
})

const emit = defineEmits(['resume', 'view-feedback', 'edit-feedback', 'select-result', 'offer'])
const userStore = useUserStore()
const { roles, departmentCode } = storeToRefs(userStore)
const isCeo = computed(() => roles.value.some(isCeoRole))
const isManagement = computed(
  () => roles.value.includes('qt_mgmt') || roles.value.some(isCeoRole),
)
function choice(row, order) {
  return row.choices?.find((item) => item.choiceOrder === order)
}

function roundState(track) {
  if (!track) return null
  if (props.roundId === 2 && !track.rounds?.[2]?.advanced) return null
  return track.rounds?.[props.roundId] || null
}

function statusType(status) {
  return { PASS: 'success', FAIL: 'danger', WAITING: 'warning', PENDING: 'info' }[status] || 'info'
}

function isPlainResult(status) {
  return status === 'PASS' || status === 'FAIL'
}

function availableTracks(row) {
  return (row.choices || []).filter((track) => {
    if (props.roundId === 2 && !track.rounds?.[2]?.advanced) return false
    return isCeo.value || track.department === departmentCode.value
  })
}

</script>

<template>
  <ElTable
    v-loading="loading"
    class="recruitment-table"
    :data="rows"
    size="small"
    row-key="applicationId"
    empty-text="暂无符合条件的候选人"
  >
    <ElTableColumn label="姓名" min-width="135">
      <template #default="{ row }">
        <div class="candidate-identity"><strong>{{ row.name }}</strong><span>{{ row.studentNo }}</span></div>
      </template>
    </ElTableColumn>
    <ElTableColumn label="专业/班级" min-width="165">
      <template #default="{ row }">
        <div class="candidate-identity"><strong>{{ row.major }}</strong><span>{{ row.className }}</span></div>
      </template>
    </ElTableColumn>
    <ElTableColumn v-for="order in [1, 2]" :key="order" :label="order === 1 ? '第一志愿' : '第二志愿'" min-width="150">
      <template #default="{ row }">
        <div v-if="choice(row, order)" class="candidate-choice">
          <span class="candidate-choice__department" :class="`choice-${order}`">
            {{ departmentLabels[choice(row, order).department] || choice(row, order).department }}
          </span>
          <StatusTag
            v-if="roundState(choice(row, order))"
            class="candidate-choice__status"
            :class="{
              'is-plain-result': isPlainResult(roundState(choice(row, order)).status),
            }"
            :type="statusType(roundState(choice(row, order)).status)"
          >
            {{ resultLabels[roundState(choice(row, order)).status] }}
          </StatusTag>
          <span v-else class="candidate-choice__inactive">未晋级</span>
        </div>
      </template>
    </ElTableColumn>
    <ElTableColumn prop="appliedAt" label="投递时间" min-width="155" />
    <ElTableColumn label="操作" min-width="260" fixed="right">
      <template #default="{ row }">
        <div class="recruitment-table__actions">
          <ElButton
            link
            class="recruitment-table__link is-primary is-underlined"
            @click="emit('resume', row)"
          >
            阅览简历
          </ElButton>
          <ElButton
            v-if="availableTracks(row).length"
            link
            class="recruitment-table__link is-underlined"
            @click="emit('view-feedback', row)"
          >
            查看面评
          </ElButton>
          <ElButton
            v-if="availableTracks(row).length"
            link
            class="recruitment-table__link is-primary is-underlined"
            @click="emit('edit-feedback', row)"
          >
            编辑面评
          </ElButton>
          <ElButton
            v-if="roundId === 1 && isCeo && availableTracks(row).length"
            link
            class="recruitment-table__link feedback-dialog__result--pass"
            data-test="candidate-result-pass"
            @click="emit('select-result', row, 'PASS')"
          >
            Pass
          </ElButton>
          <ElButton
            v-if="roundId === 1 && isCeo && availableTracks(row).length"
            link
            class="recruitment-table__link feedback-dialog__result--out"
            data-test="candidate-result-out"
            @click="emit('select-result', row, 'FAIL')"
          >
            Out
          </ElButton>
          <PermissionButton
            v-if="roundId === 2 && isManagement && availableTracks(row).length"
            link
            class="recruitment-table__link is-primary is-underlined"
            :permissions="'qt:interview:admin:offer'"
            @click="emit('offer', row)"
          >
            是否录用
          </PermissionButton>
        </div>
      </template>
    </ElTableColumn>
  </ElTable>
</template>
