<template>
  <mobile-shell active="contacts" title="通讯录">
    <div class="search-box">
      <i class="el-icon-search" />
      <input v-model.trim="query.nickName" type="search" placeholder="搜索姓名或部门..." @keyup.enter="fetchUsers" />
      <button type="button" @click="fetchUsers">搜索</button>
    </div>

    <section class="contact-summary">
      <strong>{{ users.length }}</strong>
      <span>位成员</span>
      <small>成员信息来自系统用户档案</small>
    </section>

    <div v-loading="loading" class="contact-list">
      <article v-for="user in users" :key="user.userId" class="contact-card">
        <div class="avatar">{{ initials(user) }}</div>
        <div class="contact-main">
          <h3>{{ user.nickName || user.userName }}</h3>
          <p>{{ user.memberDepartment || (user.dept && user.dept.deptName) || 'Quanta' }}</p>
          <div class="contact-meta">
            <span v-if="user.memberTitle">{{ user.memberTitle }}</span>
            <span v-if="user.memberCohort">{{ user.memberCohort }}</span>
          </div>
        </div>
        <a v-if="user.phonenumber" class="contact-call" :href="'tel:' + user.phonenumber" aria-label="拨打电话">
          <i class="el-icon-phone" />
        </a>
      </article>
      <div v-if="!loading && !users.length" class="empty-state">
        <i class="el-icon-user" />
        <span>暂无成员数据</span>
      </div>
    </div>
  </mobile-shell>
</template>

<script>
import MobileShell from '@/components/MobileShell.vue'
import { listLabMembers } from '@/api/qt'

export default {
  name: 'Contacts',
  components: { MobileShell },
  data() {
    return {
      loading: false,
      users: [],
      query: {
        pageNum: 1,
        pageSize: 30,
        nickName: ''
      }
    }
  },
  created() {
    this.fetchUsers()
  },
  methods: {
    fetchUsers() {
      this.loading = true
      listLabMembers(this.query).then(res => {
        this.users = res.rows || []
      }).catch(() => {
        this.users = []
      }).finally(() => {
        this.loading = false
      })
    },
    initials(user) {
      const name = (user.nickName || user.userName || 'Q').trim()
      return name.slice(0, 1).toUpperCase()
    }
  }
}
</script>

<style lang="scss" scoped>
.search-box {
  height: 44px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #f1f1f1;
  display: grid;
  grid-template-columns: 34px 1fr 52px;
  align-items: center;
  padding: 0 10px 0 14px;
  color: #999;
}

.search-box input {
  border: none;
  outline: none;
  background: transparent;
  min-width: 0;
  color: #333;
}

.search-box button {
  border: none;
  background: #ffedd4;
  color: #fdaf32;
  border-radius: 13px;
  height: 26px;
  font-weight: 800;
}

.contact-summary {
  margin: 18px 0;
  min-height: 104px;
  border-radius: 22px;
  background: #fff;
  padding: 20px;
  display: flex;
  flex-wrap: wrap;
  align-content: center;
  gap: 8px 10px;
  box-shadow: 0 8px 26px rgba(15, 23, 42, 0.04);
}

.contact-summary strong {
  font-size: 36px;
  line-height: 40px;
  color: #fdaf32;
}

.contact-summary span {
  align-self: flex-end;
  font-weight: 900;
  margin-bottom: 4px;
}

.contact-summary small {
  flex-basis: 100%;
  color: #888;
}

.contact-list {
  min-height: 360px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contact-card {
  min-height: 76px;
  padding: 14px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid #f3f3f3;
  display: grid;
  grid-template-columns: 48px 1fr 36px;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 18px;
  background: #ffedd4;
  color: #fdaf32;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-size: 18px;
}

.contact-main {
  min-width: 0;
}

.contact-main h3 {
  margin: 0 0 3px;
  font-size: 16px;
  line-height: 22px;
  font-weight: 900;
}

.contact-main p {
  margin: 0;
  color: #888;
  font-size: 12px;
  line-height: 18px;
}

.contact-meta {
  margin-top: 5px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.contact-meta span {
  padding: 2px 8px;
  border-radius: 10px;
  background: #f7f7f7;
  color: #777;
  font-size: 11px;
}

.contact-call {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  background: #fdaf32;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
}
</style>
