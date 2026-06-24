<template>
  <header class="topbar">
    <div class="page-title"><h1>答题结果</h1><p>本次练习已提交，统计将异步刷新。</p></div>
    <div class="top-actions"><el-button @click="$router.push('/history')">查看历史</el-button><el-button type="primary" @click="$router.push('/exam-setup')">再练一套</el-button><UserDropdown /></div>
  </header>
  <main class="main">
    <section class="grid grid-4">
      <div class="card metric"><div class="metric-label">正确率</div><div class="metric-value">{{ rate }}%</div><div class="metric-note">自动判分题和自评题合计</div></div>
      <div class="card metric"><div class="metric-label">正确数</div><div class="metric-value">{{ result.correctCount || 0 }}</div></div>
      <div class="card metric"><div class="metric-label">总题数</div><div class="metric-value">{{ result.totalCount || 0 }}</div></div>
      <div class="card metric"><div class="metric-label">用时</div><div class="metric-value">{{ result.durationSeconds || 0 }}s</div></div>
    </section>
  </main>
</template>

<script setup>
import { computed, ref } from 'vue'
import UserDropdown from '../components/UserDropdown.vue'

const result = ref(JSON.parse(sessionStorage.getItem('examResult') || '{}'))
const rate = computed(() => result.value.totalCount ? Math.round(result.value.correctCount * 100 / result.value.totalCount) : 0)
</script>
