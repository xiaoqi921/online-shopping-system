<template>
  <div class="admin-layout">
    <!-- 顶部导航栏 -->
    <nav class="topnav">
      <div class="topnav-inner">
        <div class="topnav-brand">
          <div class="brand-icon">Q</div>
          <span class="brand-text">Q管理系统 <em>管理员控制台</em></span>
        </div>
        <div class="topnav-right">
          <div class="admin-badge">
            <span class="admin-dot"></span>
            <span>{{ user?.username }}</span>
          </div>
          <button @click="logout" class="btn-logout">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
              <path fill-rule="evenodd" d="M10 12.5a.5.5 0 0 1-.5.5h-8a.5.5 0 0 1-.5-.5v-9a.5.5 0 0 1 .5-.5h8a.5.5 0 0 1 .5.5v2a.5.5 0 0 0 1 0v-2A1.5 1.5 0 0 0 9.5 2h-8A1.5 1.5 0 0 0 0 3.5v9A1.5 1.5 0 0 0 1.5 14h8a1.5 1.5 0 0 0 1.5-1.5v-2a.5.5 0 0 0-1 0v2z"/>
              <path fill-rule="evenodd" d="M15.854 8.354a.5.5 0 0 0 0-.708l-3-3a.5.5 0 0 0-.708.708L14.293 7.5H5.5a.5.5 0 0 0 0 1h8.793l-2.147 2.146a.5.5 0 0 0 .708.708l3-3z"/>
            </svg>
            退出登录
          </button>
        </div>
      </div>
    </nav>

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 页面标题 & 统计 -->
      <div class="page-header">
        <div>
          <h1 class="page-title">商品管理</h1>
          <p class="page-subtitle">管理店铺中的所有商品，支持增删改查与实时搜索</p>
        </div>
        <div class="stats-row">
          <div class="stat-card">
            <span class="stat-num">{{ products.length }}</span>
            <span class="stat-label">商品总数</span>
          </div>
          <div class="stat-card">
            <span class="stat-num">{{ lowStockCount }}</span>
            <span class="stat-label">低库存</span>
          </div>
          <div class="stat-card">
            <span class="stat-num">{{ categories.length }}</span>
            <span class="stat-label">分类数</span>
          </div>
        </div>
      </div>

      <!-- 工具栏：搜索 + 筛选 + 添加 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <div class="search-wrap">
            <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
              <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1.007 1.007 0 0 0-.115-.099zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0z"/>
            </svg>
            <input
              v-model="searchName"
              @input="debouncedSearch"
              type="text"
              placeholder="搜索商品名称..."
              class="search-input"
            />
            <button v-if="searchName" @click="clearSearch" class="search-clear">✕</button>
          </div>
          <select v-model="filterCategory" @change="fetchProducts(true)" class="filter-select">
            <option value="">全部分类</option>
            <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
          </select>
        </div>
        <button @click="openAddModal" class="btn-primary">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
            <path d="M8 4a.5.5 0 0 1 .5.5v3h3a.5.5 0 0 1 0 1h-3v3a.5.5 0 0 1-1 0v-3h-3a.5.5 0 0 1 0-1h3v-3A.5.5 0 0 1 8 4z"/>
          </svg>
          添加商品
        </button>
      </div>

      <!-- 加载中骨架 -->
      <div v-if="loading" class="skeleton-wrap">
        <div v-for="i in 4" :key="i" class="skeleton-row"></div>
      </div>

      <!-- 商品表格 -->
      <div v-else-if="filteredProducts.length > 0" class="table-wrap">
        <table class="product-table">
          <thead>
            <tr>
              <th style="width:70px">图片</th>
              <th>商品名称</th>
              <th>分类</th>
              <th style="width:110px">价格</th>
              <th style="width:100px">库存</th>
              <th style="width:150px">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in pagedProducts" :key="product.id" class="table-row">
              <td>
                <div class="img-cell">
                  <img
                    :src="product.imageUrl || fallbackImg"
                    :alt="product.name"
                    @error="e => e.target.src = fallbackImg"
                    class="product-img"
                  />
                </div>
              </td>
              <td>
                <div class="product-name">{{ product.name }}</div>
                <div class="product-desc">{{ product.description }}</div>
              </td>
              <td>
                <span class="category-tag">{{ product.category || '其他' }}</span>
              </td>
              <td class="price-cell">¥{{ Number(product.price).toFixed(2) }}</td>
              <td>
                <span :class="['stock-badge', product.stock <= 10 ? 'stock-low' : 'stock-ok']">
                  {{ product.stock }}
                </span>
              </td>
              <td>
                <div class="action-btns">
                  <button @click="openEditModal(product)" class="btn-edit" title="编辑">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="currentColor" viewBox="0 0 16 16">
                      <path d="M12.854.146a.5.5 0 0 0-.707 0L10.5 1.793 14.207 5.5l1.647-1.646a.5.5 0 0 0 0-.708l-3-3zm.646 6.061L9.793 2.5 3.293 9H3.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.207l6.5-6.5zm-7.468 7.468A.5.5 0 0 1 6 13.5V13h-.5a.5.5 0 0 1-.5-.5V12h-.5a.5.5 0 0 1-.5-.5V11h-.5a.5.5 0 0 1-.5-.5V10h-.5a.499.499 0 0 1-.175-.032l-.179.178a.5.5 0 0 0-.11.168l-2 5a.5.5 0 0 0 .65.65l5-2a.5.5 0 0 0 .168-.11l.178-.178z"/>
                    </svg>
                    编辑
                  </button>
                  <button @click="confirmDelete(product)" class="btn-delete" title="删除">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="currentColor" viewBox="0 0 16 16">
                      <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                      <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z"/>
                    </svg>
                    删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- 分页 -->
        <div class="pagination" v-if="totalPages > 1">
          <button @click="currentPage--" :disabled="currentPage === 1" class="page-btn">‹ 上一页</button>
          <button
            v-for="p in totalPages" :key="p"
            @click="currentPage = p"
            :class="['page-btn', p === currentPage ? 'page-btn-active' : '']"
          >{{ p }}</button>
          <button @click="currentPage++" :disabled="currentPage === totalPages" class="page-btn">下一页 ›</button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">📦</div>
        <p class="empty-title">没有找到商品</p>
        <p class="empty-sub">{{ searchName || filterCategory ? '尝试修改搜索条件' : '点击"添加商品"创建第一个商品' }}</p>
        <button v-if="searchName || filterCategory" @click="resetFilters" class="btn-secondary">清除筛选</button>
      </div>
    </main>

    <!-- ===== 添加/编辑商品弹窗 ===== -->
    <transition name="modal-fade">
      <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal-box">
          <div class="modal-header">
            <h2 class="modal-title">{{ editingProduct ? '编辑商品' : '添加新商品' }}</h2>
            <button @click="closeModal" class="modal-close">✕</button>
          </div>

          <div class="modal-body">
            <!-- 图片预览 -->
            <div class="img-preview-wrap" v-if="form.imageUrl">
              <img :src="form.imageUrl" @error="e => e.target.style.display='none'" class="img-preview" alt="预览" />
            </div>

            <div class="form-grid">
              <div class="form-group full">
                <label class="form-label">商品名称 <span class="required">*</span></label>
                <input
                  v-model="form.name"
                  type="text"
                  :class="['form-input', errors.name ? 'input-error' : '']"
                  placeholder="请输入商品名称"
                  maxlength="100"
                />
                <span v-if="errors.name" class="error-msg">{{ errors.name }}</span>
              </div>

              <div class="form-group full">
                <label class="form-label">商品描述</label>
                <textarea
                  v-model="form.description"
                  rows="3"
                  class="form-input form-textarea"
                  placeholder="请输入商品描述（可选）"
                ></textarea>
              </div>

              <div class="form-group">
                <label class="form-label">价格（¥）<span class="required">*</span></label>
                <input
                  v-model="form.price"
                  type="number"
                  step="0.01"
                  min="0"
                  :class="['form-input', errors.price ? 'input-error' : '']"
                  placeholder="0.00"
                />
                <span v-if="errors.price" class="error-msg">{{ errors.price }}</span>
              </div>

              <div class="form-group">
                <label class="form-label">库存数量 <span class="required">*</span></label>
                <input
                  v-model="form.stock"
                  type="number"
                  min="0"
                  :class="['form-input', errors.stock ? 'input-error' : '']"
                  placeholder="0"
                />
                <span v-if="errors.stock" class="error-msg">{{ errors.stock }}</span>
              </div>

              <div class="form-group">
                <label class="form-label">商品分类</label>
                <select v-model="form.category" class="form-input">
                  <option value="">-- 请选择分类 --</option>
                  <option value="电子产品">电子产品</option>
                  <option value="家居办公">家居办公</option>
                  <option value="服饰鞋包">服饰鞋包</option>
                  <option value="食品饮料">食品饮料</option>
                  <option value="运动户外">运动户外</option>
                  <option value="美妆护肤">美妆护肤</option>
                  <option value="其他">其他</option>
                </select>
              </div>

              <div class="form-group">
                <label class="form-label">图片链接</label>
                <input
                  v-model="form.imageUrl"
                  type="text"
                  class="form-input"
                  placeholder="https://..."
                />
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button @click="closeModal" class="btn-cancel">取消</button>
            <button @click="saveProduct" :disabled="saving" class="btn-primary">
              <span v-if="saving" class="spinner"></span>
              {{ saving ? '保存中...' : (editingProduct ? '保存修改' : '添加商品') }}
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- ===== 删除确认弹窗 ===== -->
    <transition name="modal-fade">
      <div v-if="deletingProduct" class="modal-overlay" @click.self="deletingProduct = null">
        <div class="modal-box modal-sm">
          <div class="delete-icon-wrap">
            <div class="delete-icon">🗑️</div>
          </div>
          <h2 class="delete-title">确认删除</h2>
          <p class="delete-desc">确定要删除商品 <strong>「{{ deletingProduct.name }}」</strong> 吗？此操作不可恢复。</p>
          <div class="modal-footer">
            <button @click="deletingProduct = null" class="btn-cancel">取消</button>
            <button @click="doDelete" :disabled="deleting" class="btn-danger">
              <span v-if="deleting" class="spinner"></span>
              {{ deleting ? '删除中...' : '确认删除' }}
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- ===== Toast 通知 ===== -->
    <transition name="toast-slide">
      <div v-if="toast.show" :class="['toast', `toast-${toast.type}`]">
        <span class="toast-icon">{{ toast.type === 'success' ? '✅' : '❌' }}</span>
        {{ toast.message }}
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const BASE = 'http://localhost:8080/api/admin/products'
const router = useRouter()
const user = ref(JSON.parse(localStorage.getItem('user')))

// 数据
const products = ref([])
const categories = ref([])
const loading = ref(false)
const saving = ref(false)
const deleting = ref(false)

// 搜索 & 筛选
const searchName = ref('')
const filterCategory = ref('')
let searchTimer = null

// 分页
const currentPage = ref(1)
const pageSize = 8

// 弹窗状态
const showModal = ref(false)
const editingProduct = ref(null)
const deletingProduct = ref(null)

// 表单
const defaultForm = () => ({ name: '', description: '', price: '', stock: '', imageUrl: '', category: '' })
const form = ref(defaultForm())
const errors = ref({})

// Toast
const toast = ref({ show: false, type: 'success', message: '' })

// 备用图片
const fallbackImg = 'https://placehold.co/80x80/e2e8f0/94a3b8?text=图'

// ---- 计算属性 ----
const filteredProducts = computed(() => products.value)

const lowStockCount = computed(() => products.value.filter(p => p.stock <= 10).length)

const totalPages = computed(() => Math.ceil(filteredProducts.value.length / pageSize))

const pagedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredProducts.value.slice(start, start + pageSize)
})

// 监听总页数变化，防止删除商品后当前页超出范围
watch(totalPages, (newVal) => {
  if (currentPage.value > newVal) {
    currentPage.value = Math.max(1, newVal)
  }
})

// ---- 初始化 ----
onMounted(() => {
  if (!user.value || user.value.role !== 'admin') {
    router.push('/login')
    return
  }
  fetchProducts(false)
  fetchCategories()
})

// ---- API 方法 ----
const fetchProducts = async (resetPage = false) => {
  loading.value = true
  try {
    const params = {}
    if (searchName.value.trim()) params.name = searchName.value.trim()
    if (filterCategory.value) params.category = filterCategory.value
    const res = await axios.get(BASE, { params })
    products.value = res.data
    if (resetPage) {
      currentPage.value = 1
    }
  } catch {
    showToast('error', '获取商品列表失败，请检查后端服务')
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await axios.get(`${BASE}/categories`)
    categories.value = res.data
  } catch {
    // 忽略
  }
}

const debouncedSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => fetchProducts(true), 400)
}

const clearSearch = () => {
  searchName.value = ''
  fetchProducts(true)
}

const resetFilters = () => {
  searchName.value = ''
  filterCategory.value = ''
  fetchProducts(true)
}

// ---- 弹窗操作 ----
const openAddModal = () => {
  editingProduct.value = null
  form.value = defaultForm()
  errors.value = {}
  showModal.value = true
}

const openEditModal = (product) => {
  editingProduct.value = product
  form.value = { ...product }
  errors.value = {}
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  editingProduct.value = null
  form.value = defaultForm()
  errors.value = {}
}

// ---- 表单校验 ----
const validate = () => {
  const e = {}
  if (!form.value.name?.trim()) {
    e.name = '商品名称不能为空'
  }
  
  if (form.value.price === null || form.value.price === undefined || String(form.value.price).trim() === '') {
    e.price = '价格不能为空'
  } else {
    const price = Number(form.value.price)
    if (isNaN(price) || price < 0) e.price = '请输入有效价格（≥0）'
  }
  
  if (form.value.stock === null || form.value.stock === undefined || String(form.value.stock).trim() === '') {
    e.stock = '库存数量不能为空'
  } else {
    const stock = Number(form.value.stock)
    if (!Number.isInteger(stock) || stock < 0) e.stock = '库存必须为非负整数'
  }
  
  errors.value = e
  return Object.keys(e).length === 0
}

// ---- 保存商品 ----
const saveProduct = async () => {
  if (!validate()) return
  saving.value = true
  try {
    const payload = {
      ...form.value,
      price: Number(form.value.price),
      stock: Number(form.value.stock)
    }
    if (editingProduct.value) {
      await axios.put(`${BASE}/${editingProduct.value.id}`, payload)
      showToast('success', '商品修改成功！')
      // 立即更新本地数据，确保响应式即时渲染
      const index = products.value.findIndex(p => p.id === editingProduct.value.id)
      if (index !== -1) {
        products.value[index] = { ...products.value[index], ...payload }
      }
    } else {
      await axios.post(BASE, payload)
      showToast('success', '商品添加成功！')
    }
    closeModal()
    fetchProducts(false)
    fetchCategories()
  } catch (err) {
    const msg = err.response?.data?.name || err.response?.data?.price || err.response?.data?.stock || '操作失败，请重试'
    showToast('error', msg)
  } finally {
    saving.value = false
  }
}

// ---- 删除商品 ----
const confirmDelete = (product) => {
  deletingProduct.value = product
}

const doDelete = async () => {
  deleting.value = true
  try {
    await axios.delete(`${BASE}/${deletingProduct.value.id}`)
    showToast('success', `已成功删除「${deletingProduct.value.name}」`)
    deletingProduct.value = null
    fetchProducts(false)
    fetchCategories()
  } catch {
    showToast('error', '删除失败，请重试')
  } finally {
    deleting.value = false
  }
}

// ---- 退出登录 ----
const logout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}

// ---- Toast ----
const showToast = (type, message) => {
  toast.value = { show: true, type, message }
  setTimeout(() => { toast.value.show = false }, 3000)
}
</script>

<style scoped>
/* ---- 布局 ---- */
.admin-layout {
  min-height: 100vh;
  background: #f0f4ff;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ---- 顶部导航 ---- */
.topnav {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  box-shadow: 0 4px 20px rgba(79, 70, 229, 0.35);
  position: sticky;
  top: 0;
  z-index: 100;
}
.topnav-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.topnav-brand { display: flex; align-items: center; gap: 12px; }
.brand-icon {
  width: 36px; height: 36px;
  background: rgba(255,255,255,0.2);
  border: 2px solid rgba(255,255,255,0.4);
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-weight: 800; font-size: 18px; color: white;
}
.brand-text { color: white; font-size: 17px; font-weight: 600; }
.brand-text em { font-style: normal; opacity: 0.7; font-size: 13px; margin-left: 8px; font-weight: 400; }
.topnav-right { display: flex; align-items: center; gap: 16px; }
.admin-badge {
  display: flex; align-items: center; gap: 6px;
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.25);
  border-radius: 20px;
  padding: 5px 14px;
  color: white;
  font-size: 13px;
}
.admin-dot {
  width: 8px; height: 8px;
  background: #34d399;
  border-radius: 50%;
  display: inline-block;
}
.btn-logout {
  display: flex; align-items: center; gap: 6px;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.3);
  color: white;
  border-radius: 8px;
  padding: 7px 14px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-logout:hover { background: rgba(255,255,255,0.2); }

/* ---- 主内容 ---- */
.main-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 24px;
}

/* ---- 页面标题 ---- */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 16px;
}
.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e1b4b;
  margin: 0 0 4px 0;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.page-subtitle { color: #6b7280; font-size: 14px; margin: 0; }
.stats-row { display: flex; gap: 12px; }
.stat-card {
  background: white;
  border-radius: 12px;
  padding: 14px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 80px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  border: 1px solid #e5e7eb;
}
.stat-num { font-size: 24px; font-weight: 700; color: #4f46e5; line-height: 1; }
.stat-label { font-size: 11px; color: #9ca3af; margin-top: 4px; }

/* ---- 工具栏 ---- */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 16px;
  flex-wrap: wrap;
}
.toolbar-left { display: flex; align-items: center; gap: 12px; flex: 1; flex-wrap: wrap; }
.search-wrap {
  position: relative;
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 200px;
  max-width: 360px;
}
.search-icon {
  position: absolute;
  left: 12px;
  color: #9ca3af;
  pointer-events: none;
}
.search-input {
  width: 100%;
  padding: 10px 36px 10px 38px;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
  background: white;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.search-input:focus {
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}
.search-clear {
  position: absolute;
  right: 10px;
  background: none;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  font-size: 13px;
  line-height: 1;
  padding: 2px;
}
.search-clear:hover { color: #4f46e5; }
.filter-select {
  padding: 10px 14px;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
  background: white;
  outline: none;
  cursor: pointer;
  transition: border-color 0.2s;
  color: #374151;
}
.filter-select:focus { border-color: #4f46e5; box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1); }
.btn-primary {
  display: flex; align-items: center; gap: 6px;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: white;
  border: none;
  border-radius: 10px;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  box-shadow: 0 4px 14px rgba(79, 70, 229, 0.4);
}
.btn-primary:hover { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(79, 70, 229, 0.5); }
.btn-primary:active { transform: translateY(0); }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }

/* ---- 骨架屏 ---- */
.skeleton-wrap { display: flex; flex-direction: column; gap: 12px; }
.skeleton-row {
  height: 68px;
  background: linear-gradient(90deg, #e5e7eb 25%, #f3f4f6 50%, #e5e7eb 75%);
  background-size: 200% 100%;
  border-radius: 12px;
  animation: shimmer 1.4s infinite;
}
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ---- 表格 ---- */
.table-wrap {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.07);
  overflow: hidden;
  border: 1px solid #e5e7eb;
}
.product-table { width: 100%; border-collapse: collapse; }
.product-table thead tr {
  background: linear-gradient(135deg, #f8f7ff, #f0f4ff);
  border-bottom: 2px solid #e5e7eb;
}
.product-table th {
  padding: 14px 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.table-row { border-bottom: 1px solid #f3f4f6; transition: background 0.15s; }
.table-row:last-child { border-bottom: none; }
.table-row:hover { background: #f8f7ff; }
.product-table td { padding: 14px 16px; vertical-align: middle; }
.img-cell {
  width: 52px; height: 52px;
  border-radius: 10px;
  overflow: hidden;
  background: #f3f4f6;
  flex-shrink: 0;
}
.product-img { width: 52px; height: 52px; object-fit: cover; display: block; }
.product-name { font-weight: 600; color: #1f2937; font-size: 14px; margin-bottom: 3px; }
.product-desc {
  color: #9ca3af;
  font-size: 12px;
  max-width: 320px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.category-tag {
  display: inline-block;
  background: linear-gradient(135deg, #ede9fe, #ddd6fe);
  color: #5b21b6;
  font-size: 12px;
  font-weight: 500;
  padding: 3px 10px;
  border-radius: 20px;
}
.price-cell { font-weight: 600; color: #059669; font-size: 15px; }
.stock-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}
.stock-ok { background: #d1fae5; color: #065f46; }
.stock-low { background: #fee2e2; color: #991b1b; }
.action-btns { display: flex; gap: 8px; }
.btn-edit {
  display: flex; align-items: center; gap: 4px;
  padding: 6px 12px;
  border-radius: 7px;
  border: 1.5px solid #4f46e5;
  background: transparent;
  color: #4f46e5;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-edit:hover { background: #4f46e5; color: white; }
.btn-delete {
  display: flex; align-items: center; gap: 4px;
  padding: 6px 12px;
  border-radius: 7px;
  border: 1.5px solid #ef4444;
  background: transparent;
  color: #ef4444;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-delete:hover { background: #ef4444; color: white; }

/* ---- 分页 ---- */
.pagination {
  display: flex;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  border-top: 1px solid #f3f4f6;
}
.page-btn {
  padding: 7px 14px;
  border-radius: 8px;
  border: 1.5px solid #e5e7eb;
  background: white;
  color: #374151;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}
.page-btn:hover:not(:disabled) { border-color: #4f46e5; color: #4f46e5; }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.page-btn-active {
  background: #4f46e5;
  border-color: #4f46e5;
  color: white;
}

/* ---- 空状态 ---- */
.empty-state {
  background: white;
  border-radius: 16px;
  padding: 64px 24px;
  text-align: center;
  box-shadow: 0 4px 24px rgba(0,0,0,0.07);
  border: 1px solid #e5e7eb;
}
.empty-icon { font-size: 56px; margin-bottom: 16px; }
.empty-title { font-size: 18px; font-weight: 600; color: #374151; margin: 0 0 8px 0; }
.empty-sub { color: #9ca3af; font-size: 14px; margin: 0 0 20px 0; }
.btn-secondary {
  padding: 9px 20px;
  border-radius: 9px;
  border: 1.5px solid #e5e7eb;
  background: white;
  color: #374151;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-secondary:hover { border-color: #4f46e5; color: #4f46e5; }

/* ---- 弹窗 ---- */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: 16px;
}
.modal-box {
  background: white;
  border-radius: 20px;
  width: 100%;
  max-width: 560px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 24px 60px rgba(0,0,0,0.2);
}
.modal-sm { max-width: 400px; padding: 32px; text-align: center; }
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px 20px;
  border-bottom: 1px solid #f3f4f6;
}
.modal-title { font-size: 18px; font-weight: 700; color: #1f2937; margin: 0; }
.modal-close {
  width: 32px; height: 32px;
  border-radius: 8px;
  border: none;
  background: #f3f4f6;
  color: #6b7280;
  font-size: 14px;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
}
.modal-close:hover { background: #fee2e2; color: #ef4444; }
.modal-body { padding: 24px 28px; }
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 28px 24px;
  border-top: 1px solid #f3f4f6;
}

/* 图片预览 */
.img-preview-wrap {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}
.img-preview {
  width: 120px; height: 120px;
  border-radius: 14px;
  object-fit: cover;
  border: 2px solid #e5e7eb;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

/* 表单 */
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.form-group { display: flex; flex-direction: column; }
.form-group.full { grid-column: 1 / -1; }
.form-label { font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 6px; }
.required { color: #ef4444; margin-left: 2px; }
.form-input {
  padding: 10px 14px;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  font-family: inherit;
  background: white;
  color: #1f2937;
}
.form-input:focus {
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}
.input-error { border-color: #ef4444; }
.input-error:focus { box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1); }
.form-textarea { resize: vertical; min-height: 80px; }
.error-msg { font-size: 12px; color: #ef4444; margin-top: 4px; }

/* 按钮 */
.btn-cancel {
  padding: 10px 20px;
  border-radius: 10px;
  border: 1.5px solid #e5e7eb;
  background: white;
  color: #374151;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-cancel:hover { border-color: #9ca3af; background: #f9fafb; }
.btn-danger {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 20px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
}
.btn-danger:hover { transform: translateY(-1px); box-shadow: 0 6px 18px rgba(239, 68, 68, 0.5); }
.btn-danger:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }

/* 删除弹窗 */
.delete-icon-wrap { margin-bottom: 12px; }
.delete-icon { font-size: 48px; }
.delete-title { font-size: 20px; font-weight: 700; color: #1f2937; margin: 0 0 10px; }
.delete-desc { color: #6b7280; font-size: 14px; margin: 0 0 24px; line-height: 1.6; }
.delete-desc strong { color: #1f2937; }

/* Spinner */
.spinner {
  display: inline-block;
  width: 14px; height: 14px;
  border: 2px solid rgba(255,255,255,0.4);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ---- Toast ---- */
.toast {
  position: fixed;
  bottom: 28px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 24px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  z-index: 999;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15);
  white-space: nowrap;
}
.toast-success { background: #065f46; color: white; }
.toast-error { background: #7f1d1d; color: white; }
.toast-icon { font-size: 16px; }

/* ---- 动画 ---- */
.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.25s, transform 0.25s; }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }
.modal-fade-enter-from .modal-box, .modal-fade-leave-to .modal-box { transform: scale(0.95) translateY(10px); }
.toast-slide-enter-active, .toast-slide-leave-active { transition: all 0.3s cubic-bezier(.4,0,.2,1); }
.toast-slide-enter-from, .toast-slide-leave-to { opacity: 0; transform: translate(-50%, 20px); }
</style>
