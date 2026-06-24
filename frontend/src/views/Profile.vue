<template>
  <div class="topbar">
    <div class="page-title">
      <h1>个人信息</h1>
      <p>查看和修改你的个人资料与密码</p>
    </div>
    <UserDropdown />
  </div>
  <div class="main">
    <div class="grid grid-2">
      <div class="card">
        <div class="card-header"><h3>基本信息</h3></div>
        <div class="card-body">
          <el-form ref="profileRef" label-position="top" :model="profileForm" class="band">
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-button type="primary" :loading="profileLoading" @click="saveProfile">保存</el-button>
          </el-form>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><h3>修改密码</h3></div>
        <div class="card-body">
          <el-form ref="pwdRef" label-position="top" :model="pwdForm" :rules="pwdRules" class="band">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码（6-20个字符）" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-button type="primary" :loading="pwdLoading" @click="changePassword">修改密码</el-button>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api/client'
import UserDropdown from '../components/UserDropdown.vue'

const profileRef = ref(null)
const pwdRef = ref(null)
const profileLoading = ref(false)
const pwdLoading = ref(false)

const profileForm = reactive({ username: '', email: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validatePasswordMatch = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请确认新密码'))
  } else if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [
    { required: true, message: '旧密码不能为空', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '新密码不能为空', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validatePasswordMatch, trigger: 'blur' }
  ]
}

const loadProfile = async () => {
  const data = await api.get('/user/profile')
  profileForm.username = data.username
  profileForm.email = data.email || ''
}

const saveProfile = async () => {
  profileLoading.value = true
  try {
    await api.put('/user/profile', {
      username: profileForm.username,
      email: profileForm.email
    })
    localStorage.setItem('username', profileForm.username)
    ElMessage.success('保存成功')
  } finally {
    profileLoading.value = false
  }
}

const changePassword = async () => {
  await pwdRef.value.validate(async (valid) => {
    if (!valid) return

    pwdLoading.value = true
    try {
      await api.put('/user/password', {
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      })
      ElMessage.success('密码修改成功')
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
    } finally {
      pwdLoading.value = false
    }
  })
}

onMounted(loadProfile)
</script>