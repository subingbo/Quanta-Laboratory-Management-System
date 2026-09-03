<script setup>
defineProps({
  label: { type: String, required: true },
  value: { type: [Number, String], default: '--' },
  icon: { type: Object, required: true },
  tone: { type: String, default: 'blue' },
})
</script>

<template>
  <article class="stat-card page-card">
    <div class="stat-card__icon" :class="`stat-card__icon--${tone}`">
      <ElIcon :size="24"><component :is="icon" /></ElIcon>
    </div>
    <div>
      <strong>{{ value ?? '--' }}</strong>
      <p>{{ label }}</p>
    </div>
  </article>
</template>

<style scoped>
.stat-card {
  display: flex;
  min-height: 96px;
  padding: 20px 22px;
  align-items: center;
  gap: 17px;
}

.stat-card__icon {
  display: grid;
  width: 52px;
  height: 52px;
  color: #5c5a9c;
  background: #eef2ff;
  border-radius: 13px;
  flex: 0 0 52px;
  place-items: center;
}

.stat-card__icon--green {
  color: #32a96b;
  background: #e9faef;
}

.stat-card__icon--orange {
  color: #a86f55;
  background: #fff4e8;
}

.stat-card__icon--red {
  color: #df6a70;
  background: #ffedef;
}

.stat-card strong {
  display: block;
  color: #1f2438;
  font-size: 24px;
  line-height: 1.15;
}

.stat-card p {
  margin: 5px 0 0;
  color: #7e8597;
  font-size: 12px;
}
</style>

