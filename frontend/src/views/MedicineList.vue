<template>
  <div class="page-container">
    <van-nav-bar title="药品管理" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div v-if="medicines.length === 0" class="empty-state">
        <van-icon name="tosend" />
        <p>暂无药品，点击下方按钮添加</p>
      </div>

      <div v-else class="medicine-list">
        <div
          v-for="medicine in medicines"
          :key="medicine.id"
          class="card medicine-card"
          :class="{ 'stock-warning': medicine.stockWarning }"
          @click="goToEdit(medicine.id)"
        >
          <div class="medicine-header">
            <div class="medicine-name">{{ medicine.name }}</div>
            <van-tag v-if="medicine.stockWarning" type="warning">库存不足</van-tag>
          </div>
          <div class="medicine-spec">{{ medicine.specification }}</div>
          <div class="medicine-info">
            <span>库存: {{ medicine.stockQuantity || 0 }} 片</span>
            <span v-if="medicine.remainingDays !== null">
              | 预计可用 {{ medicine.remainingDays }} 天
            </span>
          </div>
          <div class="medicine-actions">
            <van-button size="small" plain type="danger" @click.stop="confirmDelete(medicine)">
              删除
            </van-button>
          </div>
        </div>
      </div>
    </van-pull-refresh>

    <van-button
      class="add-btn"
      type="primary"
      round
      icon="plus"
      @click="goToEdit()"
    >
      添加药品
    </van-button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { getMedicines, deleteMedicine } from '@/api'

const router = useRouter()
const medicines = ref([])
const refreshing = ref(false)

const loadMedicines = async () => {
  try {
    medicines.value = await getMedicines()
  } catch (e) {
    console.error(e)
  }
}

const onRefresh = async () => {
  await loadMedicines()
  refreshing.value = false
}

const goToEdit = (id) => {
  if (id) {
    router.push(`/medicine/edit/${id}`)
  } else {
    router.push('/medicine/edit')
  }
}

const confirmDelete = async (medicine) => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: `确定要删除"${medicine.name}"吗？`
    })
    await deleteMedicine(medicine.id)
    showToast('删除成功')
    loadMedicines()
  } catch (e) {
    // 用户取消
  }
}

onMounted(() => {
  loadMedicines()
})
</script>

<style scoped>
.medicine-list {
  padding-bottom: 80px;
}

.medicine-card {
  cursor: pointer;
}

.medicine-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.medicine-name {
  font-size: 16px;
  font-weight: 600;
}

.medicine-spec {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.medicine-info {
  color: #999;
  font-size: 12px;
  margin-bottom: 12px;
}

.medicine-actions {
  display: flex;
  gap: 8px;
}

.add-btn {
  position: fixed;
  bottom: 70px;
  left: 50%;
  transform: translateX(-50%);
  padding: 0 32px;
}
</style>
