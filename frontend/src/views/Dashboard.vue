<template>
  <header class="topbar">
    <div class="page-title"><h1>学习工作台</h1><p>查看题库、错题、练习次数和平均掌握度。</p></div>
    <div class="top-actions">
      <el-button @click="$router.push('/questions/new')">新增题目</el-button>
      <el-button type="primary" @click="$router.push('/exam-setup')">开始自测</el-button>
      <UserDropdown />
    </div>
  </header>
  <main class="main">
    <section class="grid grid-4 band">
      <div class="card metric"><div class="metric-label">题库总量</div><div class="metric-value">{{ overview.questionCount || 0 }}</div><div class="metric-note">来自当前账号</div></div>
      <div class="card metric"><div class="metric-label">练习次数</div><div class="metric-value">{{ overview.examCount || 0 }}</div><div class="metric-note">累计提交试卷</div></div>
      <div class="card metric"><div class="metric-label">待复习错题</div><div class="metric-value">{{ overview.errorCount || 0 }}</div><div class="metric-note">自动收集</div></div>
      <div class="card metric"><div class="metric-label">平均掌握度</div><div class="metric-value">{{ overview.averageMastery || 0 }}%</div><div class="metric-note">按知识点统计</div></div>
    </section>
    <section class="grid grid-2">
      <div class="card">
        <div class="card-header"><h2>快捷出卷</h2><el-button @click="$router.push('/exam-setup')">配置试卷</el-button></div>
        <div class="card-body grid grid-3">
          <el-button type="primary" @click="quickStart('random')">随机 10 题</el-button>
          <el-button @click="$router.push('/exam-setup')">按知识点</el-button>
          <el-button @click="quickStart('error')">错题重做</el-button>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><h2>薄弱知识点</h2><el-button @click="$router.push('/analytics')">查看分析</el-button></div>
        <div class="card-body">
          <div v-for="item in weakStats" :key="item.id" class="bar-row band">
            <span>#{{ item.knowledgePointId }}</span>
            <div class="progress warn"><span :style="{ width: `${item.masteryDegree || 0}%` }"></span></div>
            <strong>{{ item.masteryDegree || 0 }}%</strong>
          </div>
          <el-empty v-if="!weakStats.length" description="暂无统计，完成一次练习后生成" />
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client'
import UserDropdown from '../components/UserDropdown.vue'

const router = useRouter()
const overview = ref({})
const weakStats = computed(() => [...(overview.value.knowledgeStats || [])].sort((a, b) => Number(a.masteryDegree) - Number(b.masteryDegree)).slice(0, 3))

const load = async () => { overview.value = await api.get('/analytics/overview') }
const quickStart = async (mode) => {
  const paper = await api.post('/exams/generate', { mode, count: 10, minDifficulty: 1, maxDifficulty: 5 })
  sessionStorage.setItem('paper', JSON.stringify(paper))
  router.push('/exam')
}

onMounted(load)
</script>
