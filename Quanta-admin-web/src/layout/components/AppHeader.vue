<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Fold, Expand } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const { sidebarCollapsed } = storeToRefs(appStore)
const pageTitle = computed(() => route.meta?.title || 'Quanta 后台管理系统')

function formatDateTime(date) {
  const pad = (value) => String(value).padStart(2, '0')
  return `${date.getFullYear()}/${pad(date.getMonth() + 1)}/${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const currentTime = ref(formatDateTime(new Date()))
const timer = window.setInterval(() => {
  currentTime.value = formatDateTime(new Date())
}, 1000)

onBeforeUnmount(() => window.clearInterval(timer))
</script>

<template>
  <header class="app-header">
    <div class="app-header__title-area">
      <button class="app-header__toggle" type="button" @click="appStore.toggleSidebar">
        <ElIcon :size="20"><component :is="sidebarCollapsed ? Expand : Fold" /></ElIcon>
        <span class="sr-only">{{ sidebarCollapsed ? '展开侧边栏' : '折叠侧边栏' }}</span>
      </button>
      <h1>{{ pageTitle }}</h1>
    </div>
    <div class="app-header__actions">
      <ElButton
        class="app-header__identity-button"
        :icon="ArrowLeft"
        data-testid="back-to-identity"
        @click="router.push('/')"
      >
        返回选择身份
      </ElButton>
      <time>{{ currentTime }}</time>
    </div>
  </header>
</template>
