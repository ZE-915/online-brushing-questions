<template>
  <router-view v-if="isLoginPage" />
  <div v-else class="app">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">T</div>
        <div>
          <span class="brand-title">个人刷题系统</span>
        </div>
      </div>
      <nav class="nav">
        <router-link v-for="item in nav" :key="item.path" :to="item.path">
          <span class="nav-icon">{{ item.icon }}</span>{{ item.label }}
        </router-link>
      </nav>
    </aside>
    <section class="content">
      <router-view />
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isLoginPage = computed(() => route.path === '/login' || route.path === '/register')
const nav = [
  { path: '/', icon: 'D', label: '工作台' },
  { path: '/questions', icon: 'Q', label: '题库管理' },
  { path: '/subjects', icon: 'K', label: '科目知识点' },
  { path: '/exam-setup', icon: 'S', label: '开始自测' },
  { path: '/study', icon: 'B', label: '背题模式' },
  { path: '/errors', icon: '!', label: '错题本' },
  { path: '/history', icon: 'H', label: '做题历史' },
  { path: '/analytics', icon: '%', label: '学情分析' }
]
</script>
