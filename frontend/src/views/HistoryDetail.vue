<template>
  <header class="topbar">
    <div class="page-title"><h1>答题详情</h1><p>{{ exam.name }} - {{ modeLabel(exam.mode) }} - 正确率 {{ rate }}%</p></div>
    <div class="top-actions"><el-button @click="$router.push('/history')">返回历史</el-button><UserDropdown /></div>
  </header>
  <main class="main">
    <section class="grid grid-4 band">
      <div class="card metric"><div class="metric-label">正确数</div><div class="metric-value">{{ exam.correctCount || 0 }}</div></div>
      <div class="card metric"><div class="metric-label">总题数</div><div class="metric-value">{{ exam.totalCount || 0 }}</div></div>
      <div class="card metric"><div class="metric-label">正确率</div><div class="metric-value">{{ rate }}%</div></div>
      <div class="card metric"><div class="metric-label">用时</div><div class="metric-value">{{ exam.durationSeconds || 0 }}s</div></div>
    </section>
    <section class="card">
      <div class="card-header"><h2>题目明细</h2></div>
      <div class="card-body">
        <div v-for="(item, idx) in answers" :key="item.questionId" class="detail-item" :class="statusClass(item.correctStatus)">
          <div class="detail-header">
            <span class="detail-no">{{ idx + 1 }}</span>
            <span class="detail-type">{{ typeLabel(item.type) }}</span>
            <span class="detail-status">{{ statusText(item.correctStatus) }}</span>
          </div>
          <div class="detail-stem">{{ item.stem }}</div>
          <div v-if="item.optionsJson && item.optionsJson !== '[]'" class="detail-options">
            <div v-for="opt in parseOptions(item.optionsJson)" :key="opt.key" class="detail-opt"
                 :class="{ correct: isCorrect(opt.key, item.correctAnswer), wrong: isUserWrong(opt.key, item) }">
              <span class="opt-key">{{ opt.key }}</span>
              <span class="opt-text">{{ opt.text }}</span>
            </div>
          </div>
          <div class="detail-answers">
            <span>你的答案：<b>{{ item.userAnswer || '未作答' }}</b></span>
            <span>正确答案：<b>{{ item.correctAnswer }}</b></span>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '../api/client'
import UserDropdown from '../components/UserDropdown.vue'

const route = useRoute()
const exam = ref({})
const answers = ref([])

const rate = computed(() => exam.value.totalCount ? Math.round(exam.value.correctCount * 100 / exam.value.totalCount) : 0)
const typeLabel = (t) => ({ single:'单选', multiple:'多选', blank:'填空', judge:'判断', short:'简答', calculate:'计算' }[t] || t)
const modeLabel = (m) => ({ random:'随机', knowledge:'知识点', error:'错题' }[m] || m)
const statusClass = (s) => s === 1 ? 'status-correct' : 'status-wrong'
const statusText = (s) => s === 1 ? '正确' : s === 2 ? '待评' : '错误'
const parseOptions = (json) => { try { return JSON.parse(json).filter(o => o.text) } catch { return [] } }
const isCorrect = (key, answer) => answer && answer.toUpperCase().includes(key)
const isUserWrong = (key, item) => item.userAnswer && item.userAnswer.toUpperCase().includes(key) && !isCorrect(key, item.correctAnswer)

onMounted(async () => {
  const data = await api.get(`/exams/${route.params.id}/detail`)
  exam.value = data.exam
  answers.value = data.answers
})
</script>

<style scoped>
.detail-item { padding: 16px; border-radius: 8px; margin-bottom: 12px; border: 1px solid #eee; }
.detail-item.status-correct { border-left: 4px solid #409eff; background: #f0f7ff; }
.detail-item.status-wrong { border-left: 4px solid #f56c6c; background: #fff5f5; }
.detail-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.detail-no { font-weight: 700; color: #666; }
.detail-type { font-size: 12px; background: #f0f0f0; padding: 2px 8px; border-radius: 4px; }
.detail-status { font-size: 12px; font-weight: 600; }
.status-correct .detail-status { color: #409eff; }
.status-wrong .detail-status { color: #f56c6c; }
.detail-stem { font-size: 15px; margin-bottom: 8px; }
.detail-options { display: flex; flex-direction: column; gap: 4px; margin-bottom: 8px; }
.detail-opt { display: flex; align-items: center; gap: 8px; padding: 6px 12px; border-radius: 4px; background: #fafafa; }
.detail-opt.correct { background: #e8f5e9; color: #2e7d32; font-weight: 500; }
.detail-opt.wrong { background: #ffebee; color: #c62828; text-decoration: line-through; }
.opt-key { font-weight: 700; min-width: 20px; }
.detail-answers { display: flex; gap: 24px; font-size: 13px; color: #666; margin-top: 4px; }
</style>
