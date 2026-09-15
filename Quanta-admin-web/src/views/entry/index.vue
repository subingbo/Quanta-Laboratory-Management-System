<script setup>
const entries = [
  {
    title: '新生入口',
    description: '查看招新信息、提交报名并跟进面试进度',
    to: '/login/freshman',
  },
  {
    title: '塔员入口',
    description: '进入 Quanta 成员门户和日常服务',
    to: '/login/member',
  },
  {
    title: '管理入口',
    description: '进入后台管理系统',
    to: '/admin/login',
  },
]
</script>

<template>
  <main class="entry-page">
    <section class="entry-page__content">
      <p class="entry-page__eyebrow">QUANTA DIGITAL CLUB</p>
      <h1>选择你的入口</h1>
      <p class="entry-page__intro">欢迎来到 Quanta 统一门户</p>
      <div class="entry-page__grid">
        <RouterLink v-for="entry in entries" :key="entry.to" class="entry-card" :to="entry.to">
          <span>{{ entry.title }}</span>
          <p>{{ entry.description }}</p>
          <strong>继续 →</strong>
        </RouterLink>
      </div>
    </section>
  </main>
</template>

<style scoped>
.entry-page {
  display: grid;
  min-height: 100vh;
  padding: 48px 24px;
  background:
    radial-gradient(circle at 15% 10%, rgb(255 102 0 / 14%), transparent 30%),
    #f7f7f8;
  place-items: center;
}

.entry-page__content {
  width: min(1080px, 100%);
}

.entry-page__eyebrow {
  margin: 0 0 8px;
  color: #ff6600;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.15em;
}

h1 {
  margin: 0;
  color: #202124;
  font-size: clamp(36px, 5vw, 64px);
}

.entry-page__intro {
  margin: 16px 0 36px;
  color: #6b7280;
  font-size: 18px;
}

.entry-page__grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.entry-card {
  min-height: 190px;
  padding: 28px;
  border: 1px solid #ececef;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 16px 40px rgb(31 41 55 / 7%);
  color: #1f2937;
  text-decoration: none;
}

.entry-card span {
  font-size: 24px;
  font-weight: 800;
}

.entry-card p {
  min-height: 48px;
  color: #6b7280;
  line-height: 1.6;
}

.entry-card strong {
  color: #ff6600;
}

@media (max-width: 760px) {
  .entry-page__grid {
    grid-template-columns: 1fr;
  }
}
</style>
