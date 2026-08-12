<template>
  <mobile-shell title="塔服订购" back hide-nav>
    <div v-loading="loading" class="item-list">
      <article v-for="item in items" :key="item.itemId" class="item-card" :class="{ active: selected && selected.itemId === item.itemId }" @click="selectItem(item)">
        <div class="item-image">
          <img v-if="item.effectImageUrl" :src="item.effectImageUrl" alt="" />
          <i v-else class="el-icon-shopping-bag-1" />
        </div>
        <div class="item-info">
          <h3>{{ item.itemName }}</h3>
          <p>{{ parseOptions(item.colorOptionsJson, ['黑色', '白色']).join(' / ') }}</p>
        </div>
      </article>
      <div v-if="!loading && !items.length" class="empty-state">
        <i class="el-icon-shopping-bag-1" />
        <span>暂无塔服配置</span>
      </div>
    </div>

    <section v-if="selected" class="order-panel card">
      <h2>{{ selected.itemName }}</h2>
      <label>颜色</label>
      <div class="option-row">
        <button v-for="color in colors" :key="color" type="button" :class="{ active: form.selectedColor === color }" @click="form.selectedColor = color">{{ color }}</button>
      </div>
      <label>尺码</label>
      <div class="option-row">
        <button v-for="size in sizes" :key="size" type="button" :class="{ active: form.selectedSize === size }" @click="form.selectedSize = size">{{ size }}</button>
      </div>
      <label>数量</label>
      <el-input-number v-model="form.quantity" :min="1" :max="99" />
      <label>付款方式</label>
      <el-select v-model="form.paymentConfigId" placeholder="选择付款配置">
        <el-option v-for="config in paymentConfigs" :key="config.configId" :label="config.paymentName" :value="config.configId" />
      </el-select>
      <div v-if="selectedPayment && selectedPayment.qrImageUrl" class="qr-box">
        <img :src="selectedPayment.qrImageUrl" alt="付款码" />
        <span>请扫码付款后上传截图</span>
      </div>
      <label>付款截图</label>
      <label class="upload-card">
        <i class="el-icon-camera" />
        <span>{{ proofFile ? proofFile.name : '上传付款截图' }}</span>
        <input type="file" accept="image/*" @change="onProofChange" />
      </label>
      <button class="primary-pill submit-order" type="button" :disabled="!canSubmit || submitting" @click="submitOrder">
        {{ submitting ? '提交中...' : '提交订购' }}
      </button>
    </section>
  </mobile-shell>
</template>

<script>
import MobileShell from '@/components/MobileShell.vue'
import { createClothingOrder, listClothingItems, listPaymentConfigs, uploadPaymentProof } from '@/api/qt'
import { getUser } from '@/utils/auth'
import { parseOptions, rowsOf } from '@/utils/helpers'

export default {
  name: 'Clothing',
  components: { MobileShell },
  data() {
    return {
      loading: false,
      submitting: false,
      items: [],
      paymentConfigs: [],
      selected: null,
      proofFile: null,
      form: {
        selectedColor: '',
        selectedSize: '',
        quantity: 1,
        paymentConfigId: null
      }
    }
  },
  computed: {
    colors() {
      return parseOptions(this.selected && this.selected.colorOptionsJson, ['黑色', '白色'])
    },
    sizes() {
      return parseOptions(this.selected && this.selected.sizeOptionsJson, ['S', 'M', 'L', 'XL'])
    },
    selectedPayment() {
      return this.paymentConfigs.find(item => item.configId === this.form.paymentConfigId)
    },
    canSubmit() {
      return this.selected && this.form.selectedColor && this.form.selectedSize && this.form.quantity && this.proofFile
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    parseOptions,
    fetchData() {
      this.loading = true
      Promise.all([
        listClothingItems({ pageNum: 1, pageSize: 50, status: '0' }),
        listPaymentConfigs({ pageNum: 1, pageSize: 20, enabled: '1' }).catch(() => ({ rows: [] }))
      ]).then(([items, configs]) => {
        this.items = rowsOf(items)
        this.paymentConfigs = rowsOf(configs)
        if (this.paymentConfigs.length && !this.form.paymentConfigId) {
          this.form.paymentConfigId = this.paymentConfigs[0].configId
        }
        if (this.items.length) {
          this.selectItem(this.items[0])
        }
      }).finally(() => {
        this.loading = false
      })
    },
    selectItem(item) {
      this.selected = item
      const colors = parseOptions(item.colorOptionsJson, ['黑色', '白色'])
      const sizes = parseOptions(item.sizeOptionsJson, ['S', 'M', 'L', 'XL'])
      this.form.selectedColor = colors[0] || ''
      this.form.selectedSize = sizes[0] || ''
    },
    onProofChange(event) {
      this.proofFile = event.target.files && event.target.files[0]
    },
    submitOrder() {
      if (!this.proofFile) {
        this.$message.warning('请先上传付款截图')
        return
      }
      const user = getUser() || {}
      const payment = this.selectedPayment
      const formData = new FormData()
      formData.append('file', this.proofFile)
      formData.append('subDir', 'qt/payment-proof')
      this.submitting = true
      uploadPaymentProof(formData).then(uploadRes => {
        return createClothingOrder({
          userId: user.userId,
          itemId: this.selected.itemId,
          selectedColor: this.form.selectedColor,
          selectedSize: this.form.selectedSize,
          quantity: this.form.quantity,
          paymentConfigId: this.form.paymentConfigId,
          paymentProofPath: uploadRes.fileName,
          status: 'SUBMITTED',
          itemName: this.selected.itemName,
          paymentName: payment && payment.paymentName
        })
      }).then(() => {
        this.$message.success('订购已提交')
        this.$router.push('/services/my')
      }).finally(() => {
        this.submitting = false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.item-list {
  min-height: 220px;
  display: flex;
  gap: 14px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.item-card {
  flex: 0 0 178px;
  border-radius: 20px;
  background: #fff;
  border: 1px solid #f3f3f3;
  overflow: hidden;
}

.item-card.active {
  border-color: #fdaf32;
  box-shadow: 0 12px 24px rgba(253, 175, 50, 0.18);
}

.item-image {
  height: 132px;
  background: #ffedd4;
  color: #fdaf32;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 42px;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  padding: 14px;
}

.item-info h3 {
  margin: 0 0 5px;
  font-size: 16px;
  font-weight: 900;
}

.item-info p {
  margin: 0;
  color: #888;
  font-size: 12px;
}

.order-panel {
  margin-top: 18px;
  padding: 20px;
}

.order-panel h2 {
  margin: 0 0 18px;
  font-size: 22px;
  font-weight: 900;
}

.order-panel label {
  display: block;
  margin: 14px 0 8px;
  color: #777;
  font-weight: 800;
}

.option-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.option-row button {
  height: 34px;
  min-width: 58px;
  padding: 0 14px;
  border-radius: 17px;
  border: 1px solid #ececec;
  background: #fff;
}

.option-row button.active {
  background: #ffedd4;
  color: #fdaf32;
  border-color: #fdaf32;
  font-weight: 900;
}

.el-select {
  width: 100%;
}

.qr-box {
  margin-top: 12px;
  padding: 12px;
  border-radius: 16px;
  background: #fafafa;
  text-align: center;
}

.qr-box img {
  width: 180px;
  height: 180px;
  object-fit: contain;
  display: block;
  margin: 0 auto 8px;
}

.qr-box span {
  color: #888;
  font-size: 12px;
}

.upload-card {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 48px;
  border-radius: 14px;
  border: 1px dashed #d9d9d9;
  background: #fff;
  color: #666;
  cursor: pointer;
}

.upload-card input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.submit-order {
  width: 100%;
  margin-top: 22px;
}
</style>
