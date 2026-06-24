<template>
  <header class="topbar"><div class="page-title"><h1>学情分析</h1><p>掌握度、错题数量和做题次数的基础统计。</p></div><UserDropdown /></header>
  <main class="main">
    <section class="grid grid-4 band">
      <div class="card metric"><div class="metric-label">题库总量</div><div class="metric-value">{{ data.questionCount || 0 }}</div></div>
      <div class="card metric"><div class="metric-label">练习次数</div><div class="metric-value">{{ data.examCount || 0 }}</div></div>
      <div class="card metric"><div class="metric-label">错题数</div><div class="metric-value">{{ data.errorCount || 0 }}</div></div>
      <div class="card metric"><div class="metric-label">平均掌握度</div><div class="metric-value">{{ data.averageMastery || 0 }}%</div></div>
    </section>
    <section class="card">
      <div class="card-header"><h2>知识点掌握度</h2></div>
      <div class="card-body">
        <div v-for="item in data.knowledgeStats || []" :key="item.id" class="bar-row band">
          <span>#{{ item.knowledgePointId }}</span>
          <div class="progress good"><span :style="{ width: `${item.masteryDegree || 0}%` }"></span></div>
          <strong>{{ item.masteryDegree || 0 }}%</strong>
        </div>
        <el-empty v-if="!(data.knowledgeStats || []).length" description="暂无分析数据" />
      </div>
    </section>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api/client'
import UserDropdown from '../components/UserDropdown.vue'
const data = ref({})
onMounted(async () => { data.value = await api.get('/analytics/overview') })
</script>
