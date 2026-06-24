<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <div class="user-avatar">{{ avatarText }}</div>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="profile">个人信息</el-dropdown-item>
        <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const username = computed(() => localStorage.getItem('username') || '用户')
const avatarText = computed(() => username.value.charAt(0).toUpperCase())

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>
