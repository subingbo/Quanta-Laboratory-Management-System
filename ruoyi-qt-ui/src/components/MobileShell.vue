<template>
  <div class="qt-page">
    <div class="qt-phone">
      <header class="qt-topbar">
        <button v-if="back" class="qt-icon-btn" type="button" aria-label="返回" @click="$router.back()">
          <i class="el-icon-arrow-left" />
        </button>
        <h1>{{ title }}</h1>
        <div class="qt-actions">
          <slot name="actions" />
        </div>
      </header>
      <main class="qt-content" :class="{ 'qt-content-no-nav': hideNav }">
        <slot />
      </main>
      <nav v-if="!hideNav" class="qt-nav" aria-label="Quanta 导航">
        <button
          v-for="item in navItems"
          :key="item.name"
          class="qt-nav-item"
          :class="{ active: active === item.name }"
          type="button"
          :aria-label="item.label"
          @click="$router.push(item.path)"
        >
          <i :class="item.icon" />
        </button>
      </nav>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MobileShell',
  props: {
    title: { type: String, default: 'Quanta' },
    active: { type: String, default: '' },
    back: { type: Boolean, default: false },
    hideNav: { type: Boolean, default: false }
  },
  data() {
    return {
      navItems: [
        { name: 'home', label: '首页', icon: 'el-icon-s-home', path: '/home' },
        { name: 'contacts', label: '通讯录', icon: 'el-icon-user-solid', path: '/contacts' },
        { name: 'services', label: '服务', icon: 'el-icon-menu', path: '/services' },
        { name: 'mine', label: '我的', icon: 'el-icon-s-custom', path: '/mine' }
      ]
    }
  }
}
</script>
