<template>
  <header class="topbar"><div class="page-title"><h1>做题历史</h1><p>查看每次练习的成绩、模式和用时。</p></div><UserDropdown /></header>
  <main class="main">
    <section class="card">
      <el-table :data="items" stripe>
        <el-table-column prop="name" label="试卷" />
        <el-table-column prop="mode" label="模式" width="120">
          <template #default="{ row }">{{ modeLabel(row.mode) }}</template>
        </el-table-column>
        <el-table-column label="正确率" width="120"><template #default="{ row }">{{ row.totalCount ? Math.round(row.correctCount * 100 / row.totalCount) : 0 }}%</template></el-table-column>
        <el-table-column prop="durationSeconds" label="用时秒" width="120" />
        <el-table-column prop="createTime" label="时间" width="190" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/history/${row.id}`)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api/client'
import UserDropdown from '../components/UserDropdown.vue'
const items = ref([])
const modeLabel = (mode) => ({ random: '随机', knowledge: '知识点', error: '错题' }[mode] || mode)
onMounted(async () => { items.value = await api.get('/exams/history') })
</script>
