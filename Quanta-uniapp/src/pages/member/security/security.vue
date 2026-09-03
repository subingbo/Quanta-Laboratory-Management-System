<template>
	<view class="security-page">
		<view class="top-bar"><text class="back" @click="goBack">‹</text><text class="title">账号与安全</text><view class="spacer" /></view>
		<view class="security-row danger" @click="requestDelete">注销账号</view>
		<view class="security-row" @click="requestLogout">退出登录</view>
	</view>
</template>

<script setup lang="js">
import { logout } from '../../../hooks/useLogin'
const goBack = () => uni.navigateBack()
const requestDelete = () => uni.showModal({ title: '注销账号', content: '注销后账号数据将无法恢复，确定继续吗？', confirmColor: '#ff3333', success: ({ confirm }) => confirm && uni.showToast({ title: '注销接口尚未开放', icon: 'none' }) })
const requestLogout = () => uni.showModal({ title: '退出登录', content: '确定退出当前账号吗？', success: ({ confirm }) => confirm && logout() })
</script>

<style scoped>
.security-page{min-height:100vh;box-sizing:border-box;padding:calc(24rpx + env(safe-area-inset-top)) 64rpx 80rpx;background:linear-gradient(135deg,#fff 65%,rgba(255,237,212,.25))}.top-bar{height:78rpx;display:flex;align-items:center;justify-content:space-between}.back{width:60rpx;font-size:68rpx;line-height:56rpx}.title{font-size:31rpx}.spacer{width:60rpx}.security-row{height:124rpx;margin-top:34rpx;padding:0 58rpx;border:2rpx solid #e7e7e7;border-radius:18rpx;background:#fff;display:flex;align-items:center;font-size:32rpx}.security-row:first-of-type{margin-top:72rpx}.danger{color:#ff3333}
</style>
