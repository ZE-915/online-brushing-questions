<template>
  <header class="topbar">
    <div class="page-title"><h1>出卷配置</h1><p>支持随机出卷、知识点专项和错题重做。</p></div>
    <UserDropdown />
  </header>
  <main class="main">
    <section class="card">
      <div class="card-body">
        <el-form label-position="top" :model="form">
          <el-form-item label="出卷模式">
            <el-radio-group v-model="form.mode">
              <el-radio-button label="random">随机</el-radio-button>
              <el-radio-button label="knowledge">知识点</el-radio-button>
              <el-radio-button label="error">错题</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <div class="grid grid-3">
            <el-form-item label="题目数量"><el-input-number v-model="form.count" :min="1" :max="100" /></el-form-item>
            <el-form-item label="最低难度"><el-input-number v-model="form.minDifficulty" :min="1" :max="5" /></el-form-item>
            <el-form-item label="最高难度"><el-input-number v-model="form.maxDifficulty" :min="1" :max="5" /></el-form-item>
          </div>
          <div class="grid grid-2">
            <el-form-item label="知识点"><el-select v-model="form.knowledgePointId" :disabled="form.mode !== 'knowledge'" clearable><el-option v-for="p in points" :key="p.id" :label="p.name" :value="p.id" /></el-select></el-form-item>
            <el-form-item label="限时分钟"><el-input-number v-model="form.durationMinutes" :min="0" :max="180" /></el-form-item>
          </div>
          <el-button type="primary" size="large" @click="start">开始答题</el-button>
        </el-form>
      </div>
    </section>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client'
import UserDropdown from '../components/UserDropdown.vue'

const router = useRouter()
const points = ref([])
const form = reactive({ mode: 'random', count: 10, minDifficulty: 1, maxDifficulty: 5, knowledgePointId: null, durationMinutes: 0 })
const start = async () => {
  const paper = await api.post('/exams/generate', form)
  sessionStorage.setItem('paper', JSON.stringify(paper))
  router.push('/exam')
}
onMounted(async () => { points.value = await api.get('/catalog/knowledge-points') })
</script>
