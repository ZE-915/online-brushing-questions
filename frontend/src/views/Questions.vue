<template>
  <header class="topbar">
    <div class="page-title"><h1>题库管理</h1><p>维护题目 CRUD、搜索筛选和 Excel 批量导入。</p></div>
    <div class="top-actions"><el-button @click="$router.push('/import')">Excel 导入</el-button><el-button type="primary" @click="$router.push('/questions/new')">新增题目</el-button><UserDropdown /></div>
  </header>
  <main class="main">
    <section class="toolbar band">
      <div class="filters">
        <el-input v-model="query.keyword" placeholder="题干关键词" />
        <el-select v-model="query.type" clearable placeholder="题型"><el-option v-for="t in types" :key="t.value" :label="t.label" :value="t.value" /></el-select>
        <el-select v-model="query.difficulty" clearable placeholder="难度"><el-option v-for="n in 5" :key="n" :label="`难度 ${n}`" :value="n" /></el-select>
      </div>
      <el-button type="primary" @click="load">查询</el-button>
    </section>
    <section class="card">
      <div class="card-header"><h2>题目列表</h2><span class="tag">共 {{ questions.length }} 题</span></div>
      <el-table :data="questions" stripe>
        <el-table-column prop="stem" label="题目" min-width="260" show-overflow-tooltip />
        <el-table-column prop="type" label="题型" width="100">
          <template #default="{ row }">{{ typeLabel(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="80" />
        <el-table-column label="作答" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.answerStatus === 1" type="success" size="small">答对</el-tag>
            <el-tag v-else-if="row.answerStatus === 0" type="danger" size="small">答错</el-tag>
            <el-tag v-else type="info" size="small">未答</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="answer" label="答案" width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/questions/${row.id}`)">编辑</el-button>
            <el-button link type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../api/client'
import UserDropdown from '../components/UserDropdown.vue'

const questions = ref([])
const query = reactive({ keyword: '', type: '', difficulty: null })
const types = [
  { label: '单选', value: 'single' }, { label: '多选', value: 'multiple' }, { label: '填空', value: 'blank' },
  { label: '判断', value: 'judge' }, { label: '简答', value: 'short' }, { label: '计算', value: 'calculate' }
]
const load = async () => { questions.value = await api.get('/questions', { params: query }) }
const remove = async (id) => { await api.delete(`/questions/${id}`); load() }
const typeLabel = (type) => ({ single: '单选', multiple: '多选', blank: '填空', judge: '判断', short: '简答', calculate: '计算' }[type] || type)
onMounted(load)
</script>
