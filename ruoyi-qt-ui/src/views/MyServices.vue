<template>
  <mobile-shell title="我的服务" back hide-nav>
    <el-tabs v-model="active" class="service-tabs">
      <el-tab-pane label="预约" name="reservation" />
      <el-tab-pane label="借阅" name="borrow" />
      <el-tab-pane label="订购" name="order" />
    </el-tabs>

    <div v-loading="loading" class="record-list">
      <article v-for="record in visibleRecords" :key="recordKey(record)" class="record-card">
        <h3>{{ recordTitle(record) }}</h3>
        <p>{{ recordDesc(record) }}</p>
        <span>{{ statusText(record.status) }}</span>
      </article>
      <div v-if="!loading && !visibleRecords.length" class="empty-state">
        <i class="el-icon-tickets" />
        <span>暂无记录</span>
      </div>
    </div>
  </mobile-shell>
</template>

<script>
import MobileShell from '@/components/MobileShell.vue'
import { listBookBorrows, listClothingOrders, listReservations } from '@/api/qt'
import { getUser } from '@/utils/auth'
import { rowsOf, statusText } from '@/utils/helpers'

export default {
  name: 'MyServices',
  components: { MobileShell },
  data() {
    return {
      active: 'reservation',
      loading: false,
      reservations: [],
      borrows: [],
      orders: []
    }
  },
  computed: {
    visibleRecords() {
      if (this.active === 'borrow') return this.borrows
      if (this.active === 'order') return this.orders
      return this.reservations
    }
  },
  created() {
    this.fetchRecords()
  },
  methods: {
    statusText,
    fetchRecords() {
      const user = getUser() || {}
      this.loading = true
      Promise.all([
        listReservations({ pageNum: 1, pageSize: 50, userId: user.userId }).catch(() => ({ rows: [] })),
        listBookBorrows({ pageNum: 1, pageSize: 50, userId: user.userId }).catch(() => ({ rows: [] })),
        listClothingOrders({ pageNum: 1, pageSize: 50, userId: user.userId }).catch(() => ({ rows: [] }))
      ]).then(([reservations, borrows, orders]) => {
        this.reservations = rowsOf(reservations)
        this.borrows = rowsOf(borrows)
        this.orders = rowsOf(orders)
      }).finally(() => {
        this.loading = false
      })
    },
    recordKey(record) {
      return record.reservationId || record.borrowId || record.orderId
    },
    recordTitle(record) {
      return record.workstationCode || record.bookName || record.itemName || '服务记录'
    },
    recordDesc(record) {
      if (record.workstationCode) return `${record.reserveStart || ''} 至 ${record.reserveEnd || ''}`
      if (record.bookName) return `${record.borrowTime || ''} 借出，${record.dueTime || ''} 应还`
      if (record.itemName) return `${record.selectedColor || ''} ${record.selectedSize || ''} x ${record.quantity || 1}`
      return record.createTime || ''
    }
  }
}
</script>

<style lang="scss" scoped>
.service-tabs {
  margin-top: -8px;
}

.record-list {
  min-height: 520px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-card {
  border-radius: 18px;
  background: #fff;
  padding: 16px;
  border: 1px solid #f3f3f3;
}

.record-card h3 {
  margin: 0 0 8px;
  font-size: 17px;
  font-weight: 900;
}

.record-card p {
  margin: 0 0 10px;
  color: #888;
  line-height: 20px;
}

.record-card span {
  display: inline-flex;
  height: 26px;
  align-items: center;
  padding: 0 12px;
  border-radius: 13px;
  background: #ffedd4;
  color: #fdaf32;
  font-size: 12px;
  font-weight: 900;
}
</style>
