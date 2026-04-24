<template>
  <mobile-shell title="图书借阅" back hide-nav>
    <div class="search-box">
      <i class="el-icon-search" />
      <input v-model.trim="query.bookName" type="search" placeholder="搜索书名..." @keyup.enter="fetchBooks" />
      <button type="button" @click="fetchBooks">搜索</button>
    </div>

    <section class="book-stats card">
      <strong>{{ books.length }}</strong>
      <span>本可浏览图书</span>
      <small>点击书籍即可提交借阅申请</small>
    </section>

    <div v-loading="loading" class="book-list">
      <article v-for="book in books" :key="book.bookId" class="book-card">
        <div class="book-mark">{{ (book.bookName || 'Q').slice(0, 1) }}</div>
        <div class="book-info">
          <h3>{{ book.bookName }}</h3>
          <p>{{ book.author || '未知作者' }} · {{ book.locationDesc || '未设置位置' }}</p>
          <span>可借 {{ book.availableCount || 0 }} / {{ book.totalCount || 0 }}</span>
        </div>
        <button type="button" :disabled="!book.availableCount" @click="borrow(book)">借阅</button>
      </article>
      <div v-if="!loading && !books.length" class="empty-state">
        <i class="el-icon-reading" />
        <span>暂无图书数据</span>
      </div>
    </div>
  </mobile-shell>
</template>

<script>
import MobileShell from '@/components/MobileShell.vue'
import { borrowBook, listBooks } from '@/api/qt'
import { getUser } from '@/utils/auth'
import { plusDays, rowsOf, today } from '@/utils/helpers'

export default {
  name: 'Books',
  components: { MobileShell },
  data() {
    return {
      loading: false,
      books: [],
      query: { pageNum: 1, pageSize: 50, bookName: '', status: '0' }
    }
  },
  created() {
    this.fetchBooks()
  },
  methods: {
    fetchBooks() {
      this.loading = true
      listBooks(this.query).then(res => {
        this.books = rowsOf(res)
      }).finally(() => {
        this.loading = false
      })
    },
    borrow(book) {
      const user = getUser() || {}
      borrowBook({
        bookId: book.bookId,
        userId: user.userId,
        borrowTime: today(),
        dueTime: plusDays(30),
        status: 'BORROWED'
      }).then(() => {
        this.$message.success('借阅申请已提交')
        this.$router.push('/services/my')
      })
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
}

.search-box button {
  border: none;
  background: #ffedd4;
  color: #fdaf32;
  border-radius: 13px;
  height: 26px;
  font-weight: 800;
}

.book-stats {
  margin: 16px 0;
  padding: 20px;
}

.book-stats strong {
  font-size: 34px;
  color: #fdaf32;
  margin-right: 8px;
}

.book-stats span {
  font-weight: 900;
}

.book-stats small {
  display: block;
  color: #888;
  margin-top: 6px;
}

.book-list {
  min-height: 520px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.book-card {
  min-height: 94px;
  padding: 14px;
  border-radius: 18px;
  background: #fff;
  display: grid;
  grid-template-columns: 48px 1fr 56px;
  gap: 12px;
  align-items: center;
}

.book-mark {
  width: 48px;
  height: 62px;
  border-radius: 14px;
  background: #282828;
  color: #fdaf32;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-size: 22px;
}

.book-info {
  min-width: 0;
}

.book-info h3 {
  margin: 0 0 5px;
  font-size: 16px;
  font-weight: 900;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-info p {
  margin: 0 0 6px;
  color: #888;
  font-size: 12px;
  line-height: 18px;
}

.book-info span {
  color: #fdaf32;
  font-size: 12px;
  font-weight: 900;
}

.book-card button {
  height: 34px;
  border: none;
  border-radius: 17px;
  background: #fdaf32;
  color: #fff;
  font-weight: 800;
}

.book-card button:disabled {
  background: #d7d7d7;
}
</style>
