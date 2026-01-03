<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-title">登录</div>
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="form.username"
            name="username"
            label="账号"
            placeholder="请输入账号"
            :rules="[{ required: true, message: '请输入账号' }]"
          />
          <van-field
            v-model="form.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }]"
          />
        </van-cell-group>
        <div class="remember-password">
          <van-checkbox v-model="rememberPassword">记住密码</van-checkbox>
        </div>
        <div class="login-btn">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            登录
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { login } from '@/api'
import dayjs from 'dayjs'

const router = useRouter()

const form = ref({
  username: '',
  password: ''
})

const loading = ref(false)
const rememberPassword = ref(true) // 默认记住密码

// 保存账号密码的key
const REMEMBER_KEY = 'remembered_credentials'
const REMEMBER_EXPIRE_KEY = 'remember_expire_time'

// 加载记住的账号密码
const loadRememberedCredentials = () => {
  try {
    const expireTime = localStorage.getItem(REMEMBER_EXPIRE_KEY)
    if (expireTime && dayjs().isBefore(dayjs(expireTime))) {
      const remembered = localStorage.getItem(REMEMBER_KEY)
      if (remembered) {
        const credentials = JSON.parse(remembered)
        form.value.username = credentials.username || ''
        form.value.password = credentials.password || ''
        rememberPassword.value = true
      }
    } else {
      // 过期了，清除
      localStorage.removeItem(REMEMBER_KEY)
      localStorage.removeItem(REMEMBER_EXPIRE_KEY)
    }
  } catch (e) {
    console.error('加载记住的账号密码失败', e)
  }
}

// 保存账号密码
const saveCredentials = () => {
  if (rememberPassword.value) {
    const expireTime = dayjs().add(7, 'day').toISOString() // 一周后过期
    localStorage.setItem(REMEMBER_KEY, JSON.stringify({
      username: form.value.username,
      password: form.value.password
    }))
    localStorage.setItem(REMEMBER_EXPIRE_KEY, expireTime)
  } else {
    // 不记住，清除保存的账号密码
    localStorage.removeItem(REMEMBER_KEY)
    localStorage.removeItem(REMEMBER_EXPIRE_KEY)
  }
}

const onSubmit = async () => {
  loading.value = true
  try {
    const data = await login(form.value)
    // 保存token
    localStorage.setItem('token', data.token)
    localStorage.setItem('username', data.username)
    
    // 保存账号密码（如果勾选了记住密码）
    saveCredentials()
    
    showToast('登录成功')
    // 跳转到首页
    router.push('/')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRememberedCredentials()
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 16px;
  padding: 32px 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 32px;
  color: #323233;
}

.remember-password {
  padding: 16px;
  padding-bottom: 0;
}

.login-btn {
  margin-top: 24px;
  padding: 0 16px;
}
</style>

