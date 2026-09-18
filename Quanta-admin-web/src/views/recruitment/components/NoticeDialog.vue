<script setup>
import { computed, ref, watch } from 'vue'
const props = defineProps({ modelValue: { type: Boolean, default: false }, preview: { type: Object, default: () => ({}) }, loading: { type: Boolean, default: false }, submitting: { type: Boolean, default: false } })
const emit = defineEmits(['update:modelValue', 'submit'])
const visible = computed({ get: () => props.modelValue, set: (value) => emit('update:modelValue', value) })
const file = ref(null)
const error = ref('')
const requiresQr = computed(() => Boolean(props.preview.offeredDepartment || props.preview.result === 'PASS'))
watch(() => props.modelValue, (value) => { if (value) { file.value = null; error.value = '' } })
function selectFile(event) {
  const selected = event.target.files?.[0] || null
  if (!selected) return
  if (!['image/jpeg', 'image/png'].includes(selected.type)) error.value = '仅支持 JPG、JPEG、PNG 图片'
  else if (selected.size > 5 * 1024 * 1024) error.value = '二维码图片不能超过 5 MB'
  else { file.value = selected; error.value = '' }
}
function submit() { if (requiresQr.value && !file.value) { error.value = '录用邮件必须添加群二维码'; return }; emit('submit', file.value) }
</script>
<template>
  <ElDialog v-model="visible" width="620" class="quanta-dialog notice-dialog" destroy-on-close>
    <template #header><strong>发送结果邮件</strong></template>
    <div v-loading="loading" class="notice-dialog__body">
      <dl><div><dt>收件人</dt><dd>{{ preview.recipientName || '—' }} · {{ preview.email || '—' }}</dd></div><div><dt>最终结果</dt><dd>{{ preview.offeredDepartmentLabel || preview.resultLabel || '—' }}</dd></div><div><dt>邮件主题</dt><dd>{{ preview.subject || '—' }}</dd></div></dl>
      <div class="notice-dialog__preview" aria-label="邮件模板" v-html="preview.content || ''"></div>
      <label v-if="requiresQr" class="notice-dialog__upload"><span>群二维码</span><input type="file" accept=".jpg,.jpeg,.png,image/jpeg,image/png" @change="selectFile"><small>将显示在邮件中，并作为附件备份。支持 JPG、JPEG、PNG，最大 5 MB</small></label>
      <p v-if="error" class="notice-dialog__error" role="alert">{{ error }}</p>
    </div>
    <template #footer><ElButton :disabled="submitting" @click="visible = false">取消</ElButton><ElButton data-test="send-notice" type="primary" class="notice-dialog__submit" :loading="submitting" :disabled="loading" @click="submit">确认发送</ElButton></template>
  </ElDialog>
</template>
