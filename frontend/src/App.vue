<template>
  <router-view />
  <van-tabbar v-model="active" route v-if="showTabbar">
    <van-tabbar-item to="/" icon="home-o">首页</van-tabbar-item>
    <van-tabbar-item to="/schedules" icon="clock-o">计划</van-tabbar-item>
    <van-tabbar-item to="/medicines" icon="coupon-o">药品</van-tabbar-item>
    <van-tabbar-item to="/checks" icon="notes-o">检查</van-tabbar-item>
    <van-tabbar-item to="/statistics" icon="chart-trending-o">统计</van-tabbar-item>
  </van-tabbar>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const active = ref(0)

const hiddenTabbarRoutes = ['/medicine/edit', '/check/add', '/schedule/edit']
const showTabbar = computed(() => {
  return !hiddenTabbarRoutes.some(path => route.path.startsWith(path))
})
</script>

<style>
#app {
  padding-bottom: 50px;
}
</style>
