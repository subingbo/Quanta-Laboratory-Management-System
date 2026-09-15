<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const audienceLabel = computed(() =>
  route.meta.portalAudience === 'freshman' ? '新生门户' : '塔员门户',
)
</script>

<template>
  <div class="portal-layout">
    <header class="portal-layout__header">
      <RouterLink class="portal-layout__brand" to="/">Quanta</RouterLink>
      <span>{{ audienceLabel }}</span>
    </header>
    <main class="portal-layout__main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.portal-layout {
  min-height: 100vh;
  background: #f7f7f8;
  color: #1f2937;
}

.portal-layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 64px;
  padding: 0 32px;
  border-bottom: 1px solid #ececef;
  background: #fff;
}

.portal-layout__brand {
  color: #ff6600;
  font-size: 24px;
  font-weight: 800;
  text-decoration: none;
}

.portal-layout__main {
  width: min(1200px, calc(100% - 48px));
  margin: 0 auto;
  padding: 40px 0;
}

@media (max-width: 640px) {
  .portal-layout__header {
    padding: 0 20px;
  }

  .portal-layout__main {
    width: min(100% - 32px, 1200px);
    padding: 24px 0;
  }
}
</style>
