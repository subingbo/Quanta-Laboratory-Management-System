<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import PortalHeader from './components/PortalHeader.vue'

const route = useRoute()
const audience = computed(() => route.meta.portalAudience === 'freshman' ? 'freshman' : 'member')
</script>

<template>
  <div class="portal-layout">
    <PortalHeader :audience="audience" />
    <main class="portal-layout__main portal-container">
      <RouterView />
    </main>
  </div>
</template>
