<script setup>
defineProps({
  name: { type: String, default: 'Quanta 成员' },
})
</script>

<template>
  <section class="welcome-banner">
    <div class="welcome-banner__copy">
      <h2>你好，{{ name }}！</h2>
      <p>欢迎来到 Quanta 后台管理系统，祝你今天工作顺利。</p>
    </div>
    <div class="welcome-banner__mark">Q</div>
  </section>
</template>

<style scoped>
.welcome-banner {
  position: relative;
  display: flex;
  min-height: 122px;
  padding: 28px 30px;
  color: white;
  overflow: hidden;
  background:
    radial-gradient(circle at 86% 20%, rgba(255, 173, 47, 0.12), transparent 28%),
    linear-gradient(110deg, #181d31 0%, #232942 100%);
  border-radius: var(--quanta-radius);
  box-shadow: var(--quanta-shadow);
  align-items: center;
}

.welcome-banner__copy {
  position: relative;
  z-index: 1;
}

.welcome-banner h2 {
  margin: 0 0 8px;
  font-size: 22px;
}

.welcome-banner p {
  margin: 0;
  color: #9ea6bd;
  font-size: 13px;
}

.welcome-banner__mark {
  position: absolute;
  right: 30px;
  color: rgba(255, 173, 47, 0.08);
  font-size: 112px;
  font-weight: 900;
  line-height: 1;
}
</style>

