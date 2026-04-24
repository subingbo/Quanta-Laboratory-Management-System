<template>
  <mobile-shell active="home" title="Quanta">
    <template v-slot:actions>
      <button class="plain-action" type="button" aria-label="搜索" @click="$router.push('/contacts')">
        <i class="el-icon-search" />
      </button>
      <button class="plain-action" type="button" aria-label="通知" @click="$router.push('/interview/results')">
        <i class="el-icon-bell" />
      </button>
    </template>

    <section class="hero-card" @click="$router.push('/join')">
      <div class="hero-shade" />
      <div class="hero-title">
        <h2>加入 Quanta，一起把想法做成作品</h2>
        <button type="button" aria-label="查看招新"><i class="el-icon-arrow-right" /></button>
      </div>
    </section>

    <section class="section-head">
      <h3>近期活动</h3>
      <button type="button" @click="$router.push('/join')">Join us</button>
    </section>

    <div v-loading="loading" class="activity-grid">
      <article v-for="(activity, index) in activities" :key="activity.activityId || index" class="activity-card">
        <span class="activity-index">{{ index + 1 }}</span>
        <div>
          <h4>{{ activity.title || 'Quanta 活动' }}</h4>
          <p>{{ activity.locationDesc || activity.description || '招新进行中' }}</p>
        </div>
        <button type="button" @click.stop="handleSignup(activity)">报名</button>
      </article>
      <div v-if="!loading && !activities.length" class="empty-card">
        <i class="el-icon-date" />
        <strong>暂无活动</strong>
        <span>后台发布后会同步到这里</span>
      </div>
    </div>
  </mobile-shell>
</template>

<script>
import MobileShell from '@/components/MobileShell.vue'
import { listActivities, signupActivity } from '@/api/qt'
import { getUser } from '@/utils/auth'
import { rowsOf, today } from '@/utils/helpers'

export default {
  name: 'Home',
  components: { MobileShell },
  data() {
    return {
      loading: false,
      activities: []
    }
  },
  created() {
    this.fetchActivities()
  },
  methods: {
    fetchActivities() {
      this.loading = true
      listActivities({ pageNum: 1, pageSize: 6, status: 'PUBLISHED' }).then(res => {
        this.activities = rowsOf(res)
      }).finally(() => {
        this.loading = false
      })
    },
    handleSignup(activity) {
      if (!activity || !activity.activityId) return
      const user = getUser() || {}
      signupActivity({
        activityId: activity.activityId,
        userId: user.userId,
        status: 'APPLIED',
        signupTime: today()
      }).then(() => {
        this.$message.success('报名成功')
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.hero-card {
  height: 174px;
  border-radius: 24px;
  background: linear-gradient(135deg, #4c5361, #1f2933 55%, #fdaf32);
  position: relative;
  overflow: hidden;
  box-shadow: 0 12px 26px rgba(15, 23, 42, 0.14);
}

.hero-card:before {
  content: "";
  position: absolute;
  right: 24px;
  top: 24px;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  border: 18px solid rgba(255, 255, 255, 0.18);
}

.hero-shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.02), rgba(0, 0, 0, 0.54));
}

.hero-title {
  position: absolute;
  left: 20px;
  right: 20px;
  bottom: 20px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
}

.hero-title h2 {
  max-width: 250px;
  margin: 0;
  color: #fff;
  font-size: 19px;
  line-height: 27px;
  font-weight: 900;
}

.hero-title button {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
}

.activity-grid {
  min-height: 250px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 22px;
}

.activity-card,
.empty-card {
  min-height: 156px;
  padding: 16px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #f5f5f5;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.02);
}

.activity-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.activity-index {
  width: 40px;
  height: 40px;
  border-radius: 20px;
  background: #ffedd4;
  color: #fdaf32;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
}

.activity-card h4 {
  margin: 8px 0 4px;
  font-size: 14px;
  line-height: 20px;
  font-weight: 900;
}

.activity-card p {
  margin: 0;
  color: #888;
  font-size: 12px;
  line-height: 18px;
}

.activity-card button {
  align-self: flex-start;
  height: 28px;
  padding: 0 14px;
  border: none;
  border-radius: 14px;
  background: #fdaf32;
  color: #fff;
  font-size: 12px;
  font-weight: 800;
}

.empty-card {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c8c8c;
  gap: 6px;
}
</style>
