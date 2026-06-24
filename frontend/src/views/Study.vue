<template>
  <header class="topbar">
    <div class="page-title"><h1>背题模式</h1><p>浏览所有题目，查看正确答案和解析。</p></div>
    <div class="top-actions">
      <span class="tag">第 {{ index + 1 }} / {{ filtered.length }} 题</span>
      <el-button @click="$router.push('/')">结束学习</el-button>
      <UserDropdown />
    </div>
  </header>
  <main class="main">
    <section class="toolbar band">
      <div class="filters">
        <el-select v-model="filter.subjectId" clearable placeholder="科目" @change="onFilterChange">
          <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
        <el-select v-model="filter.knowledgePointId" clearable placeholder="知识点" @change="onFilterChange">
          <el-option v-for="p in points" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-select v-model="filter.type" clearable placeholder="题型" @change="onFilterChange">
          <el-option v-for="t in types" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </div>
    </section>
    <section v-if="current" class="grid grid-2">
      <div class="card">
        <div class="card-header">
          <h2>{{ typeLabel(current.type) }}</h2>
          <span class="tag warning">难度 {{ current.difficulty }}</span>
        </div>
        <div class="card-body">
          <div class="question-stem">{{ current.stem }}</div>
          <div class="study-options">
            <div v-if="hasOptions" class="option-list">
              <div v-for="opt in options" :key="opt.key" class="study-opt" :class="{ 'study-correct': isCorrectOption(opt.key) }">
                <span class="opt-key">{{ opt.key }}</span>
                <span>{{ opt.text }}</span>
              </div>
            </div>
            <div class="study-answer"><b>正确答案：</b>{{ current.answer }}</div>
            <div v-if="current.analysis" class="study-analysis"><b>解析：</b>{{ current.analysis }}</div>
          </div>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><h2>进度</h2></div>
        <div class="card-body">
          <div class="answer-sheet band">
            <button v-for="(q, i) in filtered" :key="q.id" class="answer-no"
                    :class="{ active: i === index, done: visited.has(i) }"
                    @click="index = i">{{ i + 1 }}</button>
          </div>
          <div class="form-actions">
            <el-button :disabled="index === 0" @click="index--">上一题</el-button>
            <el-button :disabled="index === filtered.length - 1" @click="index++">下一题</el-button>
          </div>
        </div>
      </div>
    </section>
    <section v-else class="card"><div class="card-body">暂无题目，请先在题库中导入或添加题目。</div></section>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { api } from '../api/client'
import UserDropdown from '../components/UserDropdown.vue'

const allQuestions = ref([])
const subjects = ref([])
const points = ref([])
const index = ref(0)
const visited = reactive(new Set())
const filter = reactive({ subjectId: null, knowledgePointId: null, type: '' })
const types = [
  { label: '单选', value: 'single' }, { label: '多选', value: 'multiple' }, { label: '填空', value: 'blank' },
  { label: '判断', value: 'judge' }, { label: '简答', value: 'short' }, { label: '计算', value: 'calculate' }
]

const filtered = computed(() => {
  return allQuestions.value.filter(q => {
    if (filter.subjectId && q.subjectId !== filter.subjectId) return false
    if (filter.knowledgePointId && q.knowledgePointId !== filter.knowledgePointId) return false
    if (filter.type && q.type !== filter.type) return false
    return true
  })
})
const current = computed(() => filtered.value[index.value])
const options = computed(() => {
  if (!current.value?.optionsJson) return []
  try { return JSON.parse(current.value.optionsJson).filter(o => o.text) } catch { return [] }
})
const hasOptions = computed(() => ['single', 'multiple', 'judge'].includes(current.value?.type) && options.value.length > 0)
const isCorrectOption = (key) => current.value?.answer && current.value.answer.toUpperCase().includes(key)
const typeLabel = (t) => ({ single: '单选题', multiple: '多选题', blank: '填空题', judge: '判断题', short: '简答题', calculate: '计算题' }[t] || t)

const onFilterChange = () => { index.value = 0; visited.clear() }

watch(index, (val) => { visited.add(val) })

onMounted(async () => {
  const [q, s, p] = await Promise.all([
    api.get('/questions'),
    api.get('/catalog/subjects'),
    api.get('/catalog/knowledge-points')
  ])
  allQuestions.value = q
  subjects.value = s
  points.value = p
  if (filtered.value.length) visited.add(0)
})
</script>

<style scoped>
.study-options { margin-top: 12px; }
.study-opt { display: flex; align-items: center; gap: 8px; padding: 8px 12px; border-radius: 6px; margin-bottom: 6px; background: #f5f5f5; }
.study-opt.study-correct { background: #e8f5e9; color: #2e7d32; font-weight: 600; }
.opt-key { font-weight: 700; min-width: 20px; }
.study-answer { margin-top: 12px; padding: 10px; background: #e3f2fd; border-radius: 6px; color: #1565c0; }
.study-analysis { margin-top: 8px; padding: 10px; background: #fff3e0; border-radius: 6px; color: #e65100; }
.answer-no.active { outline: 2px solid #409eff; }
</style>
