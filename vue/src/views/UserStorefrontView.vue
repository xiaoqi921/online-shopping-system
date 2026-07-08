<template>
  <div class="store-layout">
    <!-- 顶部导航栏 -->
    <nav class="store-navbar">
      <div class="container nav-flex">
        <div class="nav-brand-wrap">
          <h1 class="nav-brand">Q-商城</h1>
        </div>
        <div class="nav-actions">
          <span class="username">欢迎，{{ user?.username }}</span>
          <button @click="logout" class="btn-logout">退出登录</button>
        </div>
      </div>
    </nav>

    <!-- 商品展示区 -->
    <main class="container store-main">
      <div v-if="successMessage" class="alert alert-success">
        <div class="alert-flex">
          <svg class="alert-icon" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
          </svg>
          <p class="alert-message">{{ successMessage }}</p>
        </div>
      </div>

      <div v-if="errorMessage" class="alert alert-error">
        <div class="alert-flex">
          <p class="alert-message">{{ errorMessage }}</p>
        </div>
      </div>

      <div class="product-grid">
        <div v-for="product in products" :key="product.id" class="product-card">
          <div class="product-img-wrap">
            <img :src="product.imageUrl" :alt="product.name" class="product-img" />
          </div>
          <div class="product-body">
            <div>
              <h3 class="product-title">{{ product.name }}</h3>
              <p class="product-desc">{{ product.description }}</p>
            </div>
            <div>
              <div class="product-meta">
                <p class="price-val">¥{{ product.price }}</p>
                <p class="stock-badge" :class="product.stock > 0 ? 'stock-ok' : 'stock-empty'">
                  {{ product.stock > 0 ? '库存 ' + product.stock + ' 件' : '已售罄' }}
                </p>
              </div>
              <button 
                @click="buyProduct(product)" 
                :disabled="product.stock === 0 || purchasing === product.id"
                class="btn-buy"
                :class="product.stock > 0 ? '' : 'btn-buy-disabled'"
              >
                {{ purchasing === product.id ? '处理中...' : (product.stock > 0 ? '立即购买' : '已售罄') }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const user = ref(JSON.parse(localStorage.getItem('user')))
const products = ref([])
const successMessage = ref('')
const errorMessage = ref('')
const purchasing = ref(null)

const fetchProducts = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/user/products')
    products.value = res.data
  } catch (err) {
    console.error('获取商品列表失败', err)
  }
}

onMounted(() => {
  if (!user.value || user.value.role !== 'user') {
    router.push('/login')
    return
  }
  fetchProducts()
})

const logout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}

const buyProduct = async (product) => {
  successMessage.value = ''
  errorMessage.value = ''
  purchasing.value = product.id
  
  try {
    const res = await axios.post('http://localhost:8080/api/user/products/purchase', {
      productId: product.id,
      quantity: 1
    })
    successMessage.value = `成功购买 ${product.name}！`
    fetchProducts()
  } catch (err) {
    errorMessage.value = err.response?.data || '购买失败，请稍后再试。'
  } finally {
    purchasing.value = null
    setTimeout(() => {
      successMessage.value = ''
      errorMessage.value = ''
    }, 5000)
  }
}
</script>

<style scoped>
.store-layout {
  min-height: 100vh;
  background-color: #f3f4f6;
}

.store-navbar {
  background-color: #ffffff;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  border-bottom: 1px solid #e5e7eb;
}

.container {
  max-width: 1280px;
  margin-left: auto;
  margin-right: auto;
  padding-left: 16px;
  padding-right: 16px;
}

@media (min-width: 640px) {
  .container {
    padding-left: 24px;
    padding-right: 24px;
  }
}

@media (min-width: 1024px) {
  .container {
    padding-left: 32px;
    padding-right: 32px;
  }
}

.nav-flex {
  display: flex;
  height: 64px;
  justify-content: space-between;
  align-items: center;
}

.nav-brand-wrap {
  display: flex;
  align-items: center;
}

.nav-brand {
  color: #4f46e5;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.025em;
  margin: 0;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 24px;
}

.username {
  color: #4b5563;
  font-weight: 500;
}

.btn-logout {
  color: #6b7280;
  background: none;
  border: none;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.2s ease;
  padding: 0;
  font-size: 16px;
}

.btn-logout:hover {
  color: #4f46e5;
}

.store-main {
  padding-top: 48px;
  padding-bottom: 48px;
}

.alert {
  margin-bottom: 32px;
  border-radius: 8px;
  padding: 16px;
  border-width: 1px;
  border-style: solid;
}

.alert-success {
  background-color: #f0fdf4;
  border-color: #bbf7d0;
  color: #166534;
}

.alert-error {
  background-color: #fef2f2;
  border-color: #fecaca;
  color: #991b1b;
}

.alert-flex {
  display: flex;
  align-items: center;
}

.alert-icon {
  flex-shrink: 0;
  height: 20px;
  width: 20px;
  color: #4ade80;
}

.alert-message {
  margin-left: 12px;
  font-size: 14px;
  font-weight: 500;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(1, minmax(0, 1fr));
  row-gap: 40px;
  column-gap: 24px;
}

@media (min-width: 640px) {
  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (min-width: 1024px) {
  .product-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

.product-card {
  position: relative;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px -1px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  border: 1px solid #f3f4f6;
}

.product-card:hover {
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.product-img-wrap {
  position: relative;
  width: 100%;
  height: 256px;
  background-color: #e5e7eb;
  overflow: hidden;
}

.product-card:hover .product-img {
  opacity: 0.9;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  transition: opacity 0.2s ease;
}

.product-body {
  padding: 24px;
  flex: 1 1 0%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-title {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  margin-top: 0;
  margin-bottom: 4px;
}

.product-desc {
  font-size: 14px;
  color: #6b7280;
  margin-top: 0;
  margin-bottom: 16px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.price-val {
  font-size: 24px;
  font-weight: 800;
  color: #4f46e5;
  margin: 0;
}

.stock-badge {
  font-size: 14px;
  font-weight: 500;
  padding: 2px 10px;
  border-radius: 9999px;
}

.stock-ok {
  background-color: #d1fae5;
  color: #065f46;
}

.stock-empty {
  background-color: #fee2e2;
  color: #991b1b;
}

.btn-buy {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  border: 1px solid transparent;
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 500;
  color: #ffffff;
  background-color: #4f46e5;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-buy:hover {
  background-color: #4338ca;
  transform: scale(1.02);
}

.btn-buy-disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}

.btn-buy-disabled:hover {
  background-color: #9ca3af;
  transform: none;
}
</style>
