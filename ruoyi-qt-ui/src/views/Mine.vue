<template>
  <mobile-shell active="mine" title="我的">
    <section class="profile-card">
      <div class="avatar">{{ avatarText }}</div>
      <div>
        <h2>{{ profile.nickName || profile.userName || 'Quanta 用户' }}</h2>
        <p>{{ profile.memberDepartment || profile.email || 'Nothing but professional.' }}</p>
      </div>
    </section>

    <div class="menu-list">
      <button v-for="item in menus" :key="item.path" type="button" @click="$router.push(item.path)">
        <i :class="item.icon" />
        <span>{{ item.title }}</span>
        <em>{{ item.desc }}</em>
        <i class="el-icon-arrow-right" />
      </button>
      <button type="button" class="logout" @click="handleLogout">
        <i class="el-icon-switch-button" />
        <span>退出登录</span>
        <em>返回身份选择</em>
        <i class="el-icon-arrow-right" />
      </button>
    </div>
  </mobile-shell>
</template>

<script>
import MobileShell from '@/components/MobileShell.vue'
import { getInfo, logout } from '@/api/auth'
import { clearUser, getUser, removeToken, setUser } from '@/utils/auth'

export default {
  name: 'Mine',
  components: { MobileShell },
  data() {
    return {
      profile: getUser() || {},
      menus: [
        { title: '账号与安全', desc: '修改资料与密码', icon: 'el-icon-lock', path: '/account' },
        { title: '创建名片', desc: '生成个人展示卡片', icon: 'el-icon-postcard', path: '/card' },
        { title: '我的服务', desc: '预约、借阅、订购记录', icon: 'el-icon-tickets', path: '/services/my' },
        { title: '面试进度', desc: '查看投递和结果', icon: 'el-icon-s-claim', path: '/interview/results' }
      ]
    }
  },
  computed: {
    avatarText() {
      const name = this.profile.nickName || this.profile.userName || 'Q'
      return name.slice(0, 1).toUpperCase()
    }
  },
  created() {
    getInfo().then(res => {
      this.profile = res.user || {}
      setUser(this.profile)
    }).catch(() => {})
  },
  methods: {
    handleLogout() {
      logout().catch(() => {}).finally(() => {
        removeToken()
        clearUser()
        this.$router.replace('/login')
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-card {
  min-height: 138px;
  border-radius: 24px;
  padding: 22px;
  background: #282828;
  color: #fff;
  display: grid;
  grid-template-columns: 66px 1fr;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 66px;
  height: 66px;
  border-radius: 24px;
  background: #fdaf32;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  font-weight: 900;
}

.profile-card h2 {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 900;
}

.profile-card p {
  margin: 0;
  color: rgba(255, 255, 255, 0.68);
}

.menu-list {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.menu-list button {
  min-height: 74px;
  border: none;
  border-radius: 18px;
  background: #fff;
  padding: 14px;
  display: grid;
  grid-template-columns: 36px 1fr 118px 18px;
  align-items: center;
  gap: 10px;
  text-align: left;
}

.menu-list button > i:first-child {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  background: #ffedd4;
  color: #fdaf32;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.menu-list span {
  font-weight: 900;
}

.menu-list em {
  color: #999;
  font-style: normal;
  font-size: 12px;
  text-align: right;
}

.menu-list .logout > i:first-child {
  background: #fff1f1;
  color: #f56c6c;
}
</style>
