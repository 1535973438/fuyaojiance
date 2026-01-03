<template>
  <div class="page-container home-page">
    <van-nav-bar title="服药监测">
      <template #right>
        <van-icon name="setting-o" size="20" @click="showLogoutDialog = true" />
      </template>
    </van-nav-bar>
    
    <!-- 退出登录确认对话框 -->
    <van-dialog
      v-model:show="showLogoutDialog"
      title="退出登录"
      message="确定要退出登录吗？"
      show-cancel-button
      @confirm="handleLogout"
    />
    
    <!-- 日期选择器 -->
    <van-popup v-model:show="showDatePicker" position="bottom">
      <van-date-picker
        v-model="currentDate"
        title="选择日期"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>
    
    <!-- 补服对话框 -->
    <van-popup v-model:show="showSupplementPopup" position="bottom" :style="{ height: '50%' }">
      <div class="supplement-popup">
        <div class="popup-header">
          <span class="popup-title">补服</span>
          <van-icon name="cross" @click="showSupplementPopup = false" />
        </div>
        <div class="popup-content">
          <div v-if="needInputTime" class="time-tip">
            超过设定时间两小时，请填写实际服用时间
          </div>
          <div class="time-field">
            <van-field
              v-model="supplementTimeDisplay"
              is-link
              readonly
              label="服用时间"
              placeholder="选择时间"
              @click="showTimePicker = true"
            />
          </div>
        </div>
        <div class="popup-footer">
          <van-button block type="primary" @click="confirmSupplement">确认补服</van-button>
        </div>
      </div>
    </van-popup>
    
    <!-- 时间选择器 -->
    <van-popup v-model:show="showTimePicker" position="bottom">
      <van-datetime-picker
        v-model="currentTime"
        type="datetime"
        title="选择服用时间"
        :min-date="new Date(currentSupplementRecord?.scheduledTime)"
        @confirm="onTimeConfirm"
        @cancel="showTimePicker = false"
      />
    </van-popup>

    <!-- 今日概览 -->
<!--    <div class="card summary-card">-->
<!--      <div class="summary-header">-->
<!--        <span class="date">{{ todayDate }}</span>-->
<!--        <span class="greeting">{{ greeting }}</span>-->
<!--      </div>-->
<!--      <div class="summary-stats">-->
<!--        <div class="stat-item">-->
<!--          <div class="stat-value">{{ completionRate }}%</div>-->
<!--          <div class="stat-label">本周完成率</div>-->
<!--        </div>-->
<!--        <div class="stat-divider"></div>-->
<!--        <div class="stat-item" v-if="latestCheck">-->
<!--          <div class="stat-value">{{ latestCheck.value }} <small>{{ latestCheck.unit }}</small></div>-->
<!--          <div class="stat-label">{{ latestCheck.item_name }} · {{ latestCheck.daysAgo }}</div>-->
<!--        </div>-->
<!--        <div class="stat-item" v-else>-->
<!--          <div class="stat-value">&#45;&#45;</div>-->
<!--          <div class="stat-label">暂无检查记录</div>-->
<!--        </div>-->
<!--      </div>-->
<!--    </div>-->

    <!-- 今日服药 -->
    <div class="section-header">
      <div class="header-left">
        <span class="section-title">{{ selectedDateDisplay }}</span>
        <van-icon name="calendar-o" @click="showDatePicker = true" class="date-icon" />
      </div>
      <van-button size="small" plain type="primary" @click="$router.push('/schedules')">
        管理计划
      </van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div v-if="todayMedications.length === 0" class="empty-state">
        <van-icon name="checked" />
        <p>今日暂无服药安排</p>
      </div>

      <div v-else class="medication-list">
        <div
          v-for="med in todayMedications"
          :key="med.recordId"
          class="card medication-card"
          :class="{ taken: med.status === 'TAKEN' }"
        >
          <div class="med-info">
            <div class="med-header">
              <span class="med-name">{{ med.scheduleName }}</span>
              <span class="med-time">{{ formatTime(med.scheduledTime) }}</span>
            </div>
            <div class="med-medicines">
              <div v-for="m in med.medicines" :key="m.medicineId" class="medicine-item">
                {{ m.medicineName }} {{ m.dosage }}片
              </div>
            </div>
          </div>
          <div v-if="med.status === 'PENDING'" class="action-buttons">
            <van-button
              type="primary"
              size="small"
              @click.stop="takeMed(med)"
            >
              已服
            </van-button>
            <van-button
              type="default"
              size="small"
              @click.stop="showSupplementDialog(med)"
            >
              补服
            </van-button>
          </div>
          <div v-else class="taken-info">
            <div class="taken-status">
              <van-icon name="checked" />
              <span>{{ formatTime(med.actualTime) }}</span>
            </div>
            <van-button
              type="default"
              size="small"
              @click.stop="cancelTake(med)"
            >
              撤销
            </van-button>
          </div>
        </div>
      </div>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getTodayMedications, takeMedication, cancelTakeMedication, supplementRecord, getCompletionRate, getLatestChecks, logout } from '@/api'
import dayjs from 'dayjs'

const router = useRouter()

const todayMedications = ref([])
const refreshing = ref(false)
const completionRate = ref(0)
const latestCheck = ref(null)
const showLogoutDialog = ref(false)
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const showDatePicker = ref(false)
const currentDate = ref([
  dayjs().year().toString(),
  (dayjs().month() + 1).toString().padStart(2, '0'),
  dayjs().date().toString().padStart(2, '0')
])

const showSupplementPopup = ref(false)
const showTimePicker = ref(false)
const currentSupplementRecord = ref(null)
const needInputTime = ref(false)
const supplementTimeDisplay = ref('')
const currentTime = ref(new Date())

const todayDate = computed(() => dayjs().format('M月D日 dddd'))
const greeting = computed(() => {
  const hour = dayjs().hour()
  if (hour < 6) return '夜深了，注意休息'
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const selectedDateDisplay = computed(() => {
  const date = dayjs(selectedDate.value)
  const today = dayjs()
  if (date.isSame(today, 'day')) {
    return '今日服药'
  } else if (date.isSame(today.subtract(1, 'day'), 'day')) {
    return '昨日服药'
  } else {
    return date.format('M月D日服药')
  }
})

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('HH:mm')
}

const loadData = async () => {
  try {
    const [meds, stats, checks] = await Promise.all([
      getTodayMedications(selectedDate.value),
      getCompletionRate(),
      getLatestChecks()
    ])

    todayMedications.value = meds
    completionRate.value = stats.rate || 0

    if (checks && checks.length > 0) {
      const check = checks[0]
      const daysAgo = dayjs().diff(dayjs(check.check_date), 'day')
      latestCheck.value = {
        ...check,
        daysAgo: daysAgo === 0 ? '今天' : `${daysAgo}天前`
      }
    }
  } catch (e) {
    console.error(e)
  }
}

const onDateConfirm = ({ selectedValues }) => {
  selectedDate.value = selectedValues.join('-')
  currentDate.value = selectedValues
  showDatePicker.value = false
  loadData()
}

const showSupplementDialog = (med) => {
  currentSupplementRecord.value = med
  const scheduledTime = dayjs(med.scheduledTime)
  const now = dayjs()
  const hoursDiff = now.diff(scheduledTime, 'hour')
  
  // 如果超过2小时，需要填写时间
  needInputTime.value = hoursDiff > 2
  
  if (needInputTime.value) {
    // 超过2小时，默认使用当前时间
    currentTime.value = new Date()
    supplementTimeDisplay.value = dayjs().format('YYYY-MM-DD HH:mm')
  } else {
    currentTime.value = new Date()
    supplementTimeDisplay.value = ''
  }
  
  showSupplementPopup.value = true
}

const onTimeConfirm = (value) => {
  supplementTimeDisplay.value = dayjs(value).format('YYYY-MM-DD HH:mm')
  currentTime.value = value
  showTimePicker.value = false
}

const confirmSupplement = async () => {
  if (!currentSupplementRecord.value) return
  
  try {
    const data = {
      recordId: currentSupplementRecord.value.recordId,
      scheduleId: currentSupplementRecord.value.scheduleId
    }
    
    // 如果需要填写时间，添加actualTime
    if (needInputTime.value && supplementTimeDisplay.value) {
      data.actualTime = supplementTimeDisplay.value
    }
    
    await supplementRecord(data)
    showToast('补服成功')
    showSupplementPopup.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const onRefresh = async () => {
  await loadData()
  refreshing.value = false
}

const takeMed = async (med) => {
  try {
    await takeMedication({
      recordId: med.recordId,
      scheduleId: med.scheduleId
    })
    showToast('打卡成功')
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const cancelTake = async (med) => {
  try {
    await cancelTakeMedication({
      recordId: med.recordId,
      scheduleId: med.scheduleId
    })
    showToast('撤销成功')
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleLogout = async () => {
  try {
    await logout()
    // 清除token和用户名
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    showToast('已退出登录')
    // 跳转到登录页
    router.push('/login')
  } catch (e) {
    console.error(e)
    // 即使接口失败，也清除本地数据并跳转
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    router.push('/login')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.home-page {
  background: #f5f7fa;
}

.summary-card {
  background: linear-gradient(135deg, #1989fa 0%, #07c160 100%);
  color: #fff;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.date {
  font-size: 14px;
  opacity: 0.9;
}

.greeting {
  font-size: 14px;
  opacity: 0.9;
}

.summary-stats {
  display: flex;
  align-items: center;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
}

.stat-value small {
  font-size: 14px;
  font-weight: normal;
}

.stat-label {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.3);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 16px 8px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.date-icon {
  font-size: 18px;
  color: #1989fa;
  cursor: pointer;
}

.medication-list {
  padding-bottom: 20px;
}

.medication-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.medication-card.taken {
  opacity: 0.6;
}

.med-info {
  flex: 1;
}

.med-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.med-name {
  font-size: 16px;
  font-weight: 500;
  margin-right: 8px;
}

.med-time {
  color: #1989fa;
  font-size: 14px;
}

.med-medicines {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.medicine-item {
  background: #f5f5f5;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.supplement-popup {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.popup-title {
  font-size: 16px;
  font-weight: 600;
}

.popup-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.time-tip {
  padding: 12px;
  background: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 4px;
  color: #d46b08;
  font-size: 14px;
  margin-bottom: 16px;
}

.time-field {
  margin-top: 16px;
}

.popup-footer {
  padding: 16px;
  border-top: 1px solid #eee;
}

.taken-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.taken-status {
  display: flex;
  align-items: center;
  color: #07c160;
  font-size: 14px;
}

.taken-status .van-icon {
  margin-right: 4px;
}
</style>
