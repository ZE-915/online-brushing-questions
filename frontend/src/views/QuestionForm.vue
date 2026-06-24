<template>
  <header class="topbar">
    <div class="page-title"><h1>{{ isEdit ? '编辑题目' : '新增题目' }}</h1><p>支持单选、多选、填空、判断、简答和计算题。</p></div>
    <div class="top-actions"><el-button @click="$router.push('/questions')">返回题库</el-button><el-button type="primary" @click="save">保存</el-button><UserDropdown /></div>
  </header>
  <main class="main">
    <section class="card">
      <div class="card-body">
        <el-form label-position="top" :model="form">
          <div class="grid grid-3">
            <el-form-item label="科目"><el-select v-model="form.subjectId" @change="loadPoints"><el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" /></el-select></el-form-item>
            <el-form-item label="知识点"><el-select v-model="form.knowledgePointId"><el-option v-for="p in points" :key="p.id" :label="p.name" :value="p.id" /></el-select></el-form-item>
            <el-form-item label="题型"><el-select v-model="form.type"><el-option v-for="t in types" :key="t.value" :label="t.label" :value="t.value" /></el-select></el-form-item>
          </div>
          <el-form-item label="题干"><el-input v-model="form.stem" type="textarea" :rows="5" /></el-form-item>
          <div class="grid grid-4">
            <el-form-item v-for="key in ['A','B','C','D']" :key="key" :label="`选项 ${key}`"><el-input v-model="options[key]" /></el-form-item>
          </div>
          <div class="grid grid-2">
            <el-form-item label="正确答案"><el-input v-model="form.answer" placeholder="单选 A，多选 A,C，判断 正确/错误" /></el-form-item>
            <el-form-item label="难度"><el-rate v-model="form.difficulty" /></el-form-item>
          </div>
          <el-form-item label="解析"><el-input v-model="form.analysis" type="textarea" :rows="3" /></el-form-item>
        </el-form>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../api/client'
import UserDropdown from '../components/UserDropdown.vue'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => route.params.id && route.params.id !== 'new')
const subjects = ref([])
const points = ref([])
const options = reactive({ A: '', B: '', C: '', D: '' })
const types = [
  { label: '单选', value: 'single' }, { label: '多选', value: 'multiple' }, { label: '填空', value: 'blank' },
  { label: '判断', value: 'judge' }, { label: '简答', value: 'short' }, { label: '计算', value: 'calculate' }
]
const form = reactive({ subjectId: null, knowledgePointId: null, type: 'single', stem: '', answer: '', analysis: '', difficulty: 1, optionsJson: '' })

const loadSubjects = async () => {
  subjects.value = await api.get('/catalog/subjects')
  if (!form.subjectId && subjects.value[0]) form.subjectId = subjects.value[0].id
  await loadPoints()
}
const loadPoints = async () => {
  points.value = await api.get('/catalog/knowledge-points', { params: { subjectId: form.subjectId } })
  if (!form.knowledgePointId && points.value[0]) form.knowledgePointId = points.value[0].id
}
const loadQuestion = async () => {
  if (!isEdit.value) return
  const data = await api.get(`/questions/${route.params.id}`)
  Object.assign(form, data)
  try {
    JSON.parse(data.optionsJson || '[]').forEach((item) => { options[item.key] = item.text })
  } catch {}
}
const save = async () => {
  form.optionsJson = JSON.stringify(Object.entries(options).map(([key, text]) => ({ key, text })))
  if (isEdit.value) await api.put(`/questions/${route.params.id}`, form)
  else await api.post('/questions', form)
  ElMessage.success('题目已保存')
  router.push('/questions')
}

onMounted(async () => { await loadSubjects(); await loadQuestion() })
</script>
