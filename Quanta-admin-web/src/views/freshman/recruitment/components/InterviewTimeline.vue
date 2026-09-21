<script setup>
defineProps({
  processes: {
    type: Array,
    default: () => [],
  },
})

const statusLabels = {
  pending: '待安排',
  locked: '尚未开始',
  invited: '等待面试',
  passed: '已通过',
  rejected: '未通过',
  offered: '已录用',
}
</script>

<template>
  <div v-if="processes.length" class="interview-processes">
    <article v-for="process in processes" :key="process.departmentCode" class="interview-process portal-card">
      <header>
        <p>志愿部门</p>
        <h3>{{ process.departmentName }}</h3>
      </header>
      <ol>
        <li v-for="stage in process.stages" :key="stage.key" :class="`is-${stage.status}`">
          <i aria-hidden="true"></i>
          <div>
            <strong>{{ stage.title }}</strong>
            <span>{{ statusLabels[stage.status] || stage.status }}</span>
            <small v-if="stage.feedback">{{ stage.feedback }}</small>
          </div>
        </li>
      </ol>
    </article>
  </div>
  <div v-else class="portal-empty portal-card">
    <strong>暂无面试进度</strong>
    <p>投递报名后，面试安排会在这里持续更新。</p>
  </div>
</template>
