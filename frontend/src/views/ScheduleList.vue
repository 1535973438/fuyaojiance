<template>
  <div class="page-container">
    <van-nav-bar title="服药计划" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div v-if="schedules.length === 0" class="empty-state">
        <van-icon name="clock-o" />
        <p>暂无服药计划，点击下方按钮添加</p>
      </div>

      <div v-else class="schedule-list">
        <div
          v-for="schedule in schedules"
          :key="schedule.id"
          class="card schedule-card"
          @click="goToEdit(schedule.id)"
        >
          <div class="schedule-header">
            <div class="schedule-name">{{ schedule.name }}</div>
            <van-switch v-model="schedule.isActive" size="20" @click.stop @change="toggleActive(schedule)" />
          </div>
          <div class="schedule-time">
            <van-icon name="clock-o" />
            <span>{{ schedule.scheduleTime }}</span>
            <van-tag type="primary" size="small" style="margin-left: 8px">{{ getScheduleTypeText(schedule) }}</van-tag>
          </div>
          <div class="schedule-medicines">
            <div v-for="med in schedule.medicines" :key="med.id" class="medicine-tag">
              {{ med.medicineName }} {{ med.dosage }}片
            </div>
          </div>
          <div class="schedule-actions">
            <van-button size="small" plain type="danger" @click.stop="confirmDelete(schedule)">删除</van-button>
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
      添加计划
    </van-button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { getSchedules, updateSchedule, deleteSchedule } from '@/api'

const router = useRouter()
const schedules = ref([])
const refreshing = ref(false)

const getScheduleTypeText = (schedule) => {
  const typeMap = {
    DAILY: '每天',
    INTERVAL: `每${schedule.intervalDays}天`,
    WEEKLY: `每周${schedule.weekDays?.split(',').map(d => ['', '一', '二', '三', '四', '五', '六', '日'][d]).join('、')}`
  }
  return typeMap[schedule.scheduleType] || schedule.scheduleType
}

const loadSchedules = async () => {
  try {
    schedules.value = await getSchedules()
  } catch (e) {
    console.error(e)
  }
}

const onRefresh = async () => {
  await loadSchedules()
  refreshing.value = false
}

const goToEdit = (id) => {
  if (id) {
    router.push(`/schedule/edit/${id}`)
  } else {
    router.push('/schedule/edit')
  }
}

const toggleActive = async (schedule) => {
  try {
    await updateSchedule(schedule.id, schedule)
    showToast(schedule.isActive ? '已启用' : '已停用')
  } catch (e) {
    console.error(e)
  }
}

const confirmDelete = async (schedule) => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: `确定要删除"${schedule.name}"吗？`
    })
    await deleteSchedule(schedule.id)
    showToast('删除成功')
    loadSchedules()
  } catch (e) {
    // 用户取消
  }
}

onMounted(() => {
  loadSchedules()
})
</script>

<style scoped>
.schedule-list {
  padding-bottom: 80px;
}

.schedule-card {
  cursor: pointer;
}

.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.schedule-name {
  font-size: 16px;
  font-weight: 600;
}

.schedule-time {
  display: flex;
  align-items: center;
  color: #1989fa;
  font-size: 14px;
  margin-bottom: 12px;
}

.schedule-time .van-icon {
  margin-right: 4px;
}

.schedule-medicines {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.medicine-tag {
  background: #f5f5f5;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 13px;
  color: #666;
}

.schedule-actions {
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
