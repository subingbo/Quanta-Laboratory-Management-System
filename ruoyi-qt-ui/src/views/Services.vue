<template>
  <mobile-shell active="services" title="服务">
    <section class="service-hero">
      <h2>实验室服务</h2>
      <p>预约工位、借阅图书、订购塔服，一页处理。</p>
    </section>

    <div class="service-grid">
      <button v-for="item in services" :key="item.path" class="service-card" type="button" @click="$router.push(item.path)">
        <span class="service-icon" :class="item.tone"><i :class="item.icon" /></span>
        <strong>{{ item.title }}</strong>
        <small>{{ item.desc }}</small>
        <em>{{ item.stat }}</em>
      </button>
    </div>

    <section class="quick-panel">
      <h3>我的服务</h3>
      <p>查看当前预约、借阅与订购记录。</p>
      <button type="button" @click="$router.push('/services/my')">查看记录</button>
    </section>
  </mobile-shell>
</template>

<script>
import MobileShell from '@/components/MobileShell.vue'
import { listBooks, listClothingItems, listReservations, listWorkstations } from '@/api/qt'
import { getUser } from '@/utils/auth'
import { rowsOf } from '@/utils/helpers'

export default {
  name: 'Services',
  components: { MobileShell },
  data() {
    return {
      stats: { workstations: 0, books: 0, clothes: 0, mine: 0 }
    }
  },
  computed: {
    services() {
      return [
        { title: '工位预约', desc: '选择空闲工位与预约日期', icon: 'el-icon-office-building', tone: 'tone-orange', stat: `${this.stats.workstations} 个工位`, path: '/services/reservation' },
        { title: '图书借阅', desc: '搜索书名、ISBN 或位置', icon: 'el-icon-reading', tone: 'tone-blue', stat: `${this.stats.books} 本图书`, path: '/services/books' },
        { title: '塔服订购', desc: '选择款式、颜色与尺码', icon: 'el-icon-shopping-bag-1', tone: 'tone-green', stat: `${this.stats.clothes} 款可选`, path: '/services/clothing' },
        { title: '我的服务', desc: '追踪状态与历史记录', icon: 'el-icon-tickets', tone: 'tone-gray', stat: `${this.stats.mine} 条记录`, path: '/services/my' }
      ]
    }
  },
  created() {
    this.fetchStats()
  },
  methods: {
    fetchStats() {
      const user = getUser() || {}
      Promise.all([
        listWorkstations({ pageNum: 1, pageSize: 1, status: '0' }).catch(() => ({ rows: [] })),
        listBooks({ pageNum: 1, pageSize: 1, status: '0' }).catch(() => ({ rows: [] })),
        listClothingItems({ pageNum: 1, pageSize: 1, status: '0' }).catch(() => ({ rows: [] })),
        listReservations({ pageNum: 1, pageSize: 99, userId: user.userId }).catch(() => ({ rows: [] }))
      ]).then(([workstations, books, clothes, mine]) => {
        this.stats.workstations = workstations.total || rowsOf(workstations).length
        this.stats.books = books.total || rowsOf(books).length
        this.stats.clothes = clothes.total || rowsOf(clothes).length
        this.stats.mine = mine.total || rowsOf(mine).length
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.service-hero {
  min-height: 152px;
  border-radius: 24px;
  background: #fff;
  padding: 24px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
  position: relative;
  overflow: hidden;
}

.service-hero:after {
  content: "";
  position: absolute;
  right: -48px;
  bottom: -62px;
  width: 170px;
  height: 170px;
  border-radius: 50%;
  background: #ffedd4;
}

.service-hero h2 {
  margin: 0 0 10px;
  font-size: 28px;
  line-height: 36px;
  font-weight: 900;
  position: relative;
  z-index: 1;
}

.service-hero p {
  margin: 0;
  max-width: 220px;
  color: #777;
  line-height: 22px;
  position: relative;
  z-index: 1;
}

.service-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.service-card {
  min-height: 178px;
  border: none;
  border-radius: 20px;
  background: #fff;
  padding: 18px;
  text-align: left;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
}

.service-icon {
  width: 44px;
  height: 44px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin-bottom: 18px;
}

.tone-orange { background: #ffedd4; color: #fdaf32; }
.tone-blue { background: #e8f2ff; color: #3478f6; }
.tone-green { background: #e8f8ef; color: #28a365; }
.tone-gray { background: #f1f1f1; color: #777; }

.service-card strong {
  font-size: 16px;
  line-height: 22px;
  font-weight: 900;
}

.service-card small {
  min-height: 38px;
  margin: 6px 0 12px;
  color: #888;
  font-size: 12px;
  line-height: 18px;
}

.service-card em {
  margin-top: auto;
  font-style: normal;
  color: #fdaf32;
  font-size: 12px;
  font-weight: 900;
}

.quick-panel {
  margin: 18px 0 4px;
  border-radius: 22px;
  background: #282828;
  color: #fff;
  padding: 22px;
}

.quick-panel h3 {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 900;
}

.quick-panel p {
  margin: 0 0 18px;
  color: rgba(255, 255, 255, 0.68);
}

.quick-panel button {
  height: 36px;
  border: none;
  border-radius: 18px;
  padding: 0 18px;
  background: #fdaf32;
  color: #fff;
  font-weight: 900;
}
</style>
