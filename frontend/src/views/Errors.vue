<template>
  <header class="topbar">
    <div class="page-title"><h1>错题本</h1><p>自动收集答错题目，可标记重点和移除。</p></div>
    <div class="top-actions"><el-button type="primary" @click="$router.push('/exam-setup')">错题重做</el-button><UserDropdown /></div>
  </header>
  <main class="main">
    <section class="card">
      <el-table :data="items" stripe>
        <el-table-column label="题目" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">{{ row.stem }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="题型" width="100">
          <template #default="{ row }">{{ typeLabel(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="wrongCount" label="错误次数" width="120" />
        <el-table-column prop="marked" label="重点" width="100"><template #default="{ row }"><el-switch v-model="row.marked" @change="mark(row)" /></template></el-table-column>
        <el-table-column prop="note" label="笔记" />
        <el-table-column label="操作" width="100"><template #default="{ row }"><el-button link type="danger" @click="remove(row.id)">移除</el-button></template></el-table-column>
      </el-table>
    </section>
  </main>
  <el-dialog v-model="detailVisible" title="题目详情" width="600">
    <div v-if="detailItem" class="error-detail">
      <div class="detail-stem">{{ detailItem.stem }}</div>
      <div v-if="detailOptions.length" class="detail-options">
        <div v-for="opt in detailOptions" :key="opt.key" class="detail-opt" :class="{ correct: isCorrect(opt.key) }">
          <span class="opt-key">{{ opt.key }}</span>
          <span>{{ opt.text }}</span>
        </div>
      </div>
      <div class="detail-answer"><b>正确答案：</b>{{ detailItem.answer }}</div>
      <div v-if="detailItem.analysis" class="detail-analysis"><b>解析：</b>{{ detailItem.analysis }}</div>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { api } from '../api/client'
import UserDropdown from '../components/UserDropdown.vue'

const items = ref([])
const detailVisible = ref(false)
const detailItem = ref(null)
const detailOptions = computed(() => {
  if (!detailItem.value?.optionsJson) return []
  try { return JSON.parse(detailItem.value.optionsJson).filter(o => o.text) } catch { return [] }
})
const isCorrect = (key) => detailItem.value?.answer && detailItem.value.answer.toUpperCase().includes(key)

const load = async () => { items.value = await api.get('/errors') }
const mark = async (row) => { await api.put(`/errors/${row.id}/mark`, null, { params: { marked: row.marked } }) }
const remove = async (id) => { await api.delete(`/errors/${id}`); load() }
const showDetail = (row) => { detailItem.value = row; detailVisible.value = true }
const typeLabel = (type) => ({ single: '单选', multiple: '多选', blank: '填空', judge: '判断', short: '简答', calculate: '计算' }[type] || type)
onMounted(load)
</script>

<style scoped>
.error-detail { padding: 8px 0; }
.detail-stem { font-size: 15px; margin-bottom: 12px; }
.detail-options { display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px; }
.detail-opt { display: flex; align-items: center; gap: 8px; padding: 8px 12px; border-radius: 6px; background: #f5f5f5; }
.detail-opt.correct { background: #e8f5e9; color: #2e7d32; font-weight: 600; }
.opt-key { font-weight: 700; min-width: 20px; }
.detail-answer { padding: 10px; background: #e3f2fd; border-radius: 6px; color: #1565c0; margin-bottom: 8px; }
.detail-analysis { padding: 10px; background: #fff3e0; border-radius: 6px; color: #e65100; }
</style>
