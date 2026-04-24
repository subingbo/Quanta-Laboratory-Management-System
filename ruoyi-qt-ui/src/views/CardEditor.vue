<template>
  <mobile-shell title="创建名片" back hide-nav>
    <section class="preview-card" :style="{ background: card.background }">
      <div class="preview-avatar">{{ initials }}</div>
      <h2>{{ card.name || 'Quanta 成员' }}</h2>
      <p>{{ card.role || 'Nothing but professional.' }}</p>
      <span>{{ card.department || 'Quanta' }}</span>
    </section>

    <section class="card form-card">
      <el-input v-model.trim="card.name" placeholder="姓名" />
      <el-input v-model.trim="card.department" placeholder="部门" />
      <el-input v-model.trim="card.role" placeholder="职能 / 标语" />
      <div class="color-row">
        <button v-for="color in colors" :key="color" type="button" :style="{ background: color }" :class="{ active: card.background === color }" @click="card.background = color" />
      </div>
      <button class="primary-pill block-btn" type="button" @click="$message.success('名片已生成，可截图保存')">生成名片</button>
    </section>
  </mobile-shell>
</template>

<script>
import MobileShell from '@/components/MobileShell.vue'
import { getUser } from '@/utils/auth'

export default {
  name: 'CardEditor',
  components: { MobileShell },
  data() {
    const user = getUser() || {}
    return {
      colors: ['#282828', '#fdaf32', '#3478f6', '#28a365'],
      card: {
        name: user.nickName || user.userName || '',
        department: user.memberDepartment || 'Quanta',
        role: user.memberTitle || 'Nothing but professional.',
        background: '#282828'
      }
    }
  },
  computed: {
    initials() {
      return (this.card.name || 'Q').slice(0, 1).toUpperCase()
    }
  }
}
</script>

<style lang="scss" scoped>
.preview-card {
  min-height: 260px;
  border-radius: 28px;
  padding: 28px;
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.18);
}

.preview-avatar {
  width: 72px;
  height: 72px;
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  font-weight: 900;
  margin-bottom: 28px;
}

.preview-card h2 {
  margin: 0 0 8px;
  font-size: 30px;
  font-weight: 900;
}

.preview-card p,
.preview-card span {
  margin: 0;
  color: rgba(255, 255, 255, 0.75);
}

.form-card {
  margin-top: 18px;
  padding: 18px;
}

.form-card .el-input {
  margin-bottom: 12px;
}

.color-row {
  display: flex;
  gap: 12px;
  margin: 6px 0 18px;
}

.color-row button {
  width: 38px;
  height: 38px;
  border-radius: 19px;
  border: 3px solid #fff;
  box-shadow: 0 0 0 1px #e8e8e8;
}

.color-row button.active {
  box-shadow: 0 0 0 2px #fdaf32;
}

.block-btn {
  width: 100%;
}
</style>
