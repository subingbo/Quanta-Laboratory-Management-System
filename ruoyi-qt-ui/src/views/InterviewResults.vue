<template>
  <mobile-shell title="面试" back hide-nav>
    <section class="status-card">
      <span>{{ application.applyStatus ? statusText(application.applyStatus) : '未投递' }}</span>
      <h2>{{ application.realName || 'Quanta 招新' }}</h2>
      <p>{{ application.firstChoice || '第一志愿' }} / {{ application.secondChoice || '第二志愿' }}</p>
      <button type="button" @click="$router.push('/join')">{{ application.applicationId ? '修改投递' : '立即投递' }}</button>
    </section>

    <section class="section-title">
      <h3>面试结果</h3>
      <button type="button" @click="fetchData">刷新</button>
    </section>

    <div v-loading="loading" class="result-list">
      <article v-for="result in results" :key="result.resultId" class="result-card">
        <div>
          <h3>{{ result.roundName || '面试轮次' }}</h3>
          <p>{{ result.department || 'Quanta' }} · {{ result.interviewTime || '待安排' }}</p>
        </div>
        <span>{{ statusText(result.resultStatus) }}</span>
      </article>
      <div v-if="!loading && !results.length" class="empty-state">
        <i class="el-icon-s-claim" />
        <span>暂无面试结果</span>
      </div>
    </div>
  </mobile-shell>
</template>

<script>
import MobileShell from '@/components/MobileShell.vue'
import { getMyInterviewApplication, getMyInterviewResults } from '@/api/qt'
import { statusText } from '@/utils/helpers'

export default {
  name: 'InterviewResults',
  components: { MobileShell },
  data() {
    return {
      loading: false,
      application: {},
      results: []
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    statusText,
    fetchData() {
      this.loading = true
      Promise.all([
        getMyInterviewApplication().catch(() => ({ data: {} })),
        getMyInterviewResults().catch(() => ({ data: [] }))
      ]).then(([application, results]) => {
        const appData = application.data || {}
        this.application = appData.application || {}
        this.results = results.data || []
      }).finally(() => {
        this.loading = false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.status-card {
  border-radius: 24px;
  background: #fff;
  padding: 22px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
}

.status-card span {
  display: inline-flex;
  height: 28px;
  align-items: center;
  padding: 0 12px;
  border-radius: 14px;
  background: #ffedd4;
  color: #fdaf32;
  font-size: 12px;
  font-weight: 900;
}

.status-card h2 {
  margin: 18px 0 8px;
  font-size: 26px;
  font-weight: 900;
}

.status-card p {
  margin: 0 0 20px;
  color: #888;
}

.status-card button {
  height: 38px;
  border: none;
  border-radius: 19px;
  padding: 0 18px;
  background: #fdaf32;
  color: #fff;
  font-weight: 900;
}

.result-list {
  min-height: 420px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-card {
  min-height: 84px;
  border-radius: 18px;
  background: #fff;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.result-card h3 {
  margin: 0 0 6px;
  font-size: 17px;
  font-weight: 900;
}

.result-card p {
  margin: 0;
  color: #888;
  line-height: 20px;
}

.result-card span {
  align-self: flex-start;
  flex-shrink: 0;
  color: #fdaf32;
  font-weight: 900;
}
</style>
