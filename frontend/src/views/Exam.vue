<template>
  <header class="topbar">
    <div class="page-title"><h1>在线答题</h1><p>第 {{ index + 1 }} / {{ questions.length }} 题</p></div>
    <div class="top-actions">
      <span class="tag">已用时 {{ elapsed }} 秒</span>
      <el-button type="primary" @click="submit">提交答卷</el-button>
      <UserDropdown />
    </div>
  </header>
  <main class="main" v-if="current">
    <section class="grid grid-2">
      <div class="card">
        <div class="card-header"><h2>{{ typeLabel(current.type) }}</h2><span class="tag warning">难度 {{ current.difficulty }}</span></div>
        <div class="card-body">
          <div class="question-stem">{{ current.stem }}</div>
          <div v-if="['single','multiple'].includes(current.type)" class="option-list">
            <label v-for="opt in options" :key="opt.key" class="option">
              <el-checkbox v-if="current.type === 'multiple'" v-model="multiAnswers" :label="opt.key">{{ opt.key }}. {{ opt.text }}</el-checkbox>
              <el-radio v-else v-model="answers[current.id].answer" :label="opt.key">{{ opt.key }}. {{ opt.text }}</el-radio>
            </label>
          </div>
          <el-radio-group v-else-if="current.type === 'judge'" v-model="answers[current.id].answer" class="band">
            <el-radio-button label="正确">正确</el-radio-button>
            <el-radio-button label="错误">错误</el-radio-button>
          </el-radio-group>
          <el-input v-else v-model="answers[current.id].answer" type="textarea" :rows="5" placeholder="输入答案" />
          <el-checkbox v-if="['short','calculate'].includes(current.type)" v-model="answers[current.id].selfCorrect">自评为正确</el-checkbox>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><h2>答题卡</h2></div>
        <div class="card-body">
          <div class="answer-sheet band">
            <button v-for="(q, i) in questions" :key="q.id" class="answer-no" :class="{ done: answers[q.id]?.answer }" @click="index = i">{{ i + 1 }}</button>
          </div>
          <div class="form-actions">
            <el-button :disabled="index === 0" @click="index--">上一题</el-button>
            <el-button :disabled="index === questions.length - 1" @click="index++">下一题</el-button>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client'
import UserDropdown from '../components/UserDropdown.vue'

const router = useRouter()
const paper = JSON.parse(sessionStorage.getItem('paper') || '{}')
const questions = ref(paper.questions || [])
const index = ref(0)
const elapsed = ref(0)
const answers = reactive({})
let timer
const current = computed(() => questions.value[index.value])
const options = computed(() => JSON.parse(current.value?.optionsJson || '[]').filter((item) => item.text))
const multiAnswers = computed({
  get: () => (answers[current.value?.id]?.answer || '').split(',').filter(Boolean),
  set: (value) => { answers[current.value.id].answer = value.join(',') }
})
watch(current, (q) => { if (q && !answers[q.id]) answers[q.id] = { questionId: q.id, answer: '', answerSeconds: 0, selfCorrect: false } }, { immediate: true })
const typeLabel = (type) => ({ single: '单选题', multiple: '多选题', blank: '填空题', judge: '判断题', short: '简答题', calculate: '计算题' }[type] || type)
const submit = async () => {
  const result = await api.post('/exams/submit', { paperId: paper.paperId, mode: paper.mode, durationSeconds: elapsed.value, answers: Object.values(answers) })
  sessionStorage.setItem('examResult', JSON.stringify(result))
  router.push('/exam-result')
}
onMounted(() => { timer = setInterval(() => elapsed.value++, 1000) })
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>
