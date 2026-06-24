<template>
  <div class="login-page">
    <section class="login-panel">
      <div class="brand-mark">T</div>
      <h1>个人刷题系统</h1>
      <p>登录后管理题库、生成试卷并跟踪错题与掌握度。</p>
      <el-form ref="formRef" label-position="top" :model="form" :rules="rules" class="band">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <div class="form-actions">
          <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
        </div>
        <p class="form-link">没有账户？<router-link to="/register">去注册</router-link></p>
      </el-form>
    </section>
    <section class="login-visual">
      <h2>把题库、练习、错题和分析放在一个闭环里。</h2>
      <p>面向个人自用的 MVP，优先保证导题、刷题、判分和复盘流程顺畅。</p>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../api/client'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const data = await api.post('/auth/login', form)
      localStorage.setItem('token', data.token)
      localStorage.setItem('username', data.username)
      ElMessage.success('登录成功')
      router.push('/')
    } catch (error) {
      ElMessage.error(error.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}
</script>
