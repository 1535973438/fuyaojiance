<template>
  <div class="page-container">
    <van-nav-bar title="检查记录" />

    <!-- 检查项目筛选 -->
    <div class="filter-bar">
      <van-dropdown-menu>
        <van-dropdown-item v-model="selectedItemId" :options="filterOptions" @change="loadRecords" />
      </van-dropdown-menu>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div v-if="records.length === 0" class="empty-state">
        <van-icon name="description" />
        <p>暂无检查记录</p>
      </div>

      <div v-else class="record-list">
        <van-swipe-cell v-for="record in records" :key="record.id">
          <div class="card record-card" @click="viewRecord(record)">
            <div class="record-header">
              <span class="record-name">{{ record.itemName }}</span>
              <span class="record-date">{{ record.checkDate }}</span>
            </div>
            <div class="record-value" :class="{ abnormal: !record.isNormal }">
              {{ record.value }} <small>{{ record.unit }}</small>
            </div>
            <div class="record-reference" v-if="record.referenceMin || record.referenceMax">
              参考范围: {{ record.referenceMin || '-' }} ~ {{ record.referenceMax || '-' }} {{ record.unit }}
            </div>
          </div>
          <template #right>
            <van-button square type="primary" text="编辑" @click.stop="editRecord(record)" />
            <van-button square type="danger" text="删除" @click.stop="confirmDelete(record)" />
          </template>
        </van-swipe-cell>
      </div>
    </van-pull-refresh>

    <van-button
      class="add-btn"
      type="primary"
      round
      icon="plus"
      @click="goToAdd"
    >
      添加记录
    </van-button>

    <!-- 查看图片弹窗 -->
    <van-image-preview v-model:show="showImagePreview" :images="previewImages" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { getCheckItems, getCheckRecords, deleteCheckRecord } from '@/api'

const router = useRouter()
const checkItems = ref([])
const records = ref([])
const refreshing = ref(false)
const selectedItemId = ref(null)
const showImagePreview = ref(false)
const previewImages = ref([])

const filterOptions = computed(() => [
  { text: '全部检查项', value: null },
  ...checkItems.value.map(item => ({ text: item.name, value: item.id }))
])

const loadItems = async () => {
  try {
    checkItems.value = await getCheckItems()
  } catch (e) {
    console.error(e)
  }
}

const loadRecords = async () => {
  try {
    records.value = await getCheckRecords({ checkItemId: selectedItemId.value })
  } catch (e) {
    console.error(e)
  }
}

const onRefresh = async () => {
  await Promise.all([loadItems(), loadRecords()])
  refreshing.value = false
}

const goToAdd = () => {
  router.push('/check/add')
}

const viewRecord = (record) => {
  if (record.reportImage) {
    previewImages.value = [record.reportImage]
    showImagePreview.value = true
  }
}

const editRecord = (record) => {
  router.push(`/check/edit/${record.id}`)
}

const confirmDelete = async (record) => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: '确定要删除此检查记录吗？'
    })
    await deleteCheckRecord(record.id)
    showToast('删除成功')
    loadRecords()
  } catch (e) {
    // 用户取消
  }
}

onMounted(() => {
  loadItems()
  loadRecords()
})
</script>

<style scoped>
.filter-bar {
  padding: 0 16px 8px;
}

.record-list {
  padding-bottom: 80px;
}

.record-card {
  cursor: pointer;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.record-name {
  font-size: 16px;
  font-weight: 500;
}

.record-date {
  font-size: 13px;
  color: #999;
}

.record-value {
  font-size: 24px;
  font-weight: 600;
  color: #07c160;
  margin-bottom: 4px;
}

.record-value.abnormal {
  color: #ee0a24;
}

.record-value small {
  font-size: 14px;
  font-weight: normal;
  color: #666;
}

.record-reference {
  font-size: 12px;
  color: #999;
}

.add-btn {
  position: fixed;
  bottom: 70px;
  left: 50%;
  transform: translateX(-50%);
  padding: 0 32px;
}

:deep(.van-swipe-cell__right) {
  display: flex;
}
</style>
