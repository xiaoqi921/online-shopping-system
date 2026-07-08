<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>Q-管理系统</h2>
        <p>请登录您的账号</p>
      </div>
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="username" type="text" required placeholder="例如：admin 或 user1" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="password" type="password" required placeholder="••••••••" />
        </div>
        <div v-if="error" class="error-alert">
          <div>{{ error }}</div>
        </div>
        <button type="submit" class="btn-submit">
          登 录
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const username = ref('')
const password = ref('')
const error = ref('')
const router = useRouter()

const handleLogin = async () => {
  error.value = ''
  try {
    const response = await axios.post('http://localhost:8080/api/auth/login', {
      username: username.value,
      password: password.value
    })
    const user = response.data.user
    localStorage.setItem('user', JSON.stringify(user))
    if (user.role === 'admin') {
      router.push('/admin')
    } else {
      router.push('/store')
    }
  } catch (err) {
    error.value = err.response?.data || '登录失败，请检查您的用户名和密码。'
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e0e7ff 0%, #f3e8ff 50%, #fce7f3 100%);
  padding: 0 16px;
  box-sizing: border-box;
}

.login-card {
  width: 100%;
  max-width: 440px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.8);
  padding: 32px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-sizing: border-box;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h2 {
  font-size: 30px;
  font-weight: 800;
  color: #111827;
  margin: 0;
  letter-spacing: -0.025em;
}

.login-header p {
  margin-top: 8px;
  font-size: 14px;
  color: #4b5563;
  margin-bottom: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-group input {
  width: 100%;
  box-sizing: border-box;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  background-color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
  color: #111827;
  outline: none;
  transition: all 0.2s ease;
}

.form-group input:focus {
  background-color: #ffffff;
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.15);
}

.error-alert {
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 12px 16px;
}

.error-alert div {
  font-size: 14px;
  color: #b91c1c;
}

.btn-submit {
  width: 100%;
  padding: 12px 16px;
  background-color: #4f46e5;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 6px -1px rgba(79, 70, 229, 0.2), 0 2px 4px -1px rgba(79, 70, 229, 0.1);
  transition: all 0.2s ease;
  display: flex;
  justify-content: center;
  align-items: center;
}

.btn-submit:hover {
  background-color: #4338ca;
  transform: translateY(-1px);
  box-shadow: 0 10px 15px -3px rgba(79, 70, 229, 0.3), 0 4px 6px -2px rgba(79, 70, 229, 0.05);
}

.btn-submit:active {
  transform: translateY(0);
}
</style>
