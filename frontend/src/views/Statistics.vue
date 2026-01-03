<template>
  <div class="page-container">
    <van-nav-bar title="统计分析" />

    <!-- 服药完成率 -->
    <div class="card">
      <div class="card-title">服药完成率</div>
      <div class="completion-stats">
        <div class="rate-circle">
          <van-circle
            v-model:current-rate="animatedRate"
            :rate="completionRate"
            :speed="100"
            :stroke-width="80"
            :color="completionRate >= 80 ? '#07c160' : '#ee0a24'"
            layer-color="#ebedf0"
            size="100px"
          >
            <span class="rate-text">{{ completionRate }}%</span>
          </van-circle>
        </div>
        <div class="rate-info">
          <div class="rate-item">
            <span class="rate-label">已服</span>
            <span class="rate-value taken">{{ stats.taken || 0 }} 次</span>
          </div>
          <div class="rate-item">
            <span class="rate-label">漏服</span>
            <span class="rate-value missed">{{ stats.missed || 0 }} 次</span>
          </div>
          <div class="rate-item">
            <span class="rate-label">总计</span>
            <span class="rate-value">{{ stats.total || 0 }} 次</span>
          </div>
        </div>
      </div>
      <div class="period-selector">
        <van-button
          v-for="period in periods"
          :key="period.value"
          size="small"
          :type="selectedPeriod === period.value ? 'primary' : 'default'"
          @click="changePeriod(period.value)"
        >
          {{ period.label }}
        </van-button>
      </div>
    </div>

    <!-- 检查趋势 -->
    <div class="card">
      <div class="card-header">
        <div class="card-title">检查趋势</div>
        <van-dropdown-menu>
          <van-dropdown-item v-model="selectedCheckItem" :options="checkItemOptions" />
        </van-dropdown-menu>
      </div>
      <div ref="chartContainer" class="chart-container"></div>
    </div>

    <!-- 最近检查结果 -->
    <div class="card">
      <div class="card-title">最近检查结果</div>
      <div v-if="latestChecks.length === 0" class="empty-tip">暂无检查记录</div>
      <div v-else class="latest-list">
        <div v-for="check in latestChecks" :key="check.id" class="latest-item">
          <div class="item-name">{{ check.item_name }}</div>
          <div class="item-value">
            <span :class="{ abnormal: isAbnormal(check) }">{{ check.value }}</span>
            <small>{{ check.unit }}</small>
          </div>
          <div class="item-date">{{ formatDate(check.check_date) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { getCompletionRate, getCheckTrends, getLatestChecks, getCheckItems } from '@/api'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

const stats = ref({})
const completionRate = computed(() => stats.value.rate || 0)
const animatedRate = ref(0)

const periods = [
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '全部', value: 'all' }
]
const selectedPeriod = ref('week')

const checkItems = ref([])
const selectedCheckItem = ref(null)
const trendData = ref([])
const latestChecks = ref([])

const chartContainer = ref(null)
let chart = null

const checkItemOptions = computed(() =>
  checkItems.value.filter(item => item.isPreset).map(item => ({
    text: item.name,
    value: item.id
  }))
)

const loadStats = async () => {
  let startDate, endDate
  const today = dayjs()

  switch (selectedPeriod.value) {
    case 'week':
      startDate = today.startOf('week').add(1, 'day').format('YYYY-MM-DD')
      endDate = today.format('YYYY-MM-DD')
      break
    case 'month':
      startDate = today.startOf('month').format('YYYY-MM-DD')
      endDate = today.format('YYYY-MM-DD')
      break
    case 'all':
      startDate = '2020-01-01'
      endDate = today.format('YYYY-MM-DD')
      break
  }

  try {
    stats.value = await getCompletionRate({ startDate, endDate })
  } catch (e) {
    console.error(e)
  }
}

const changePeriod = (period) => {
  selectedPeriod.value = period
  loadStats()
}

const loadItems = async () => {
  try {
    checkItems.value = await getCheckItems()
    const presetItems = checkItems.value.filter(item => item.isPreset)
    if (presetItems.length > 0) {
      selectedCheckItem.value = presetItems[0].id
    }
  } catch (e) {
    console.error(e)
  }
}

const loadTrends = async () => {
  if (!selectedCheckItem.value) return

  try {
    trendData.value = await getCheckTrends({ checkItemId: selectedCheckItem.value, months: 3 })
    await nextTick()
    renderChart()
  } catch (e) {
    console.error(e)
  }
}

const loadLatest = async () => {
  try {
    latestChecks.value = await getLatestChecks()
  } catch (e) {
    console.error(e)
  }
}

const renderChart = () => {
  if (!chartContainer.value) return

  if (!chart) {
    chart = echarts.init(chartContainer.value)
  }

  const item = checkItems.value.find(i => i.id === selectedCheckItem.value)
  const dates = trendData.value.map(d => d.check_date)
  const values = trendData.value.map(d => Number(d.value))

  const option = {
    grid: {
      left: 60,
      right: 20,
      top: 40,
      bottom: 40
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLabel: {
        formatter: (val) => dayjs(val).format('MM-DD'),
        rotate: 0,
        fontSize: 11
      },
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value',
      name: item?.unit || '',
      nameTextStyle: {
        fontSize: 11,
        padding: [0, 0, 0, 40]
      },
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    series: [
      {
        type: 'line',
        data: values,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          color: '#1989fa',
          width: 2
        },
        itemStyle: {
          color: '#1989fa'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(25, 137, 250, 0.3)' },
            { offset: 1, color: 'rgba(25, 137, 250, 0.05)' }
          ])
        },
        markArea: item?.referenceMin && item?.referenceMax ? {
          silent: true,
          data: [[
            {
              yAxis: Number(item.referenceMin),
              itemStyle: { color: 'rgba(7, 193, 96, 0.1)' }
            },
            { yAxis: Number(item.referenceMax) }
          ]]
        } : undefined
      }
    ],
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const p = params[0]
        return `${dayjs(p.axisValue).format('YYYY-MM-DD')}<br/>${p.value} ${item?.unit || ''}`
      }
    }
  }

  chart.setOption(option, true)
  chart.resize()
}

const isAbnormal = (check) => {
  if (check.reference_min && check.value < check.reference_min) return true
  if (check.reference_max && check.value > check.reference_max) return true
  return false
}

const formatDate = (date) => {
  const diff = dayjs().diff(dayjs(date), 'day')
  if (diff === 0) return '今天'
  if (diff === 1) return '昨天'
  return `${diff}天前`
}

const handleResize = () => {
  chart?.resize()
}

watch(selectedCheckItem, () => {
  if (selectedCheckItem.value) {
    loadTrends()
  }
})

onMounted(async () => {
  await loadItems()
  await Promise.all([loadStats(), loadTrends(), loadLatest()])
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped>
.completion-stats {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  gap: 20px;
}

.rate-circle {
  margin-right: 20px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 100px;
}

.rate-circle :deep(.van-circle) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.rate-circle :deep(.van-circle__text) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  width: 100%;
  height: 100%;
}

.rate-text {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  line-height: 1;
  margin: 0;
  padding: 0;
  text-align: center;
}

.rate-info {
  flex: 1;
}

.rate-item {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
}

.rate-label {
  color: #666;
}

.rate-value {
  font-weight: 500;
}

.rate-value.taken {
  color: #07c160;
}

.rate-value.missed {
  color: #ee0a24;
}

.period-selector {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.chart-container {
  height: 220px;
  width: 100%;
}

.empty-tip {
  text-align: center;
  color: #999;
  padding: 20px;
}

.latest-list {
  margin-top: 8px;
}

.latest-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.latest-item:last-child {
  border-bottom: none;
}

.item-name {
  flex: 1;
  font-size: 14px;
}

.item-value {
  font-size: 16px;
  font-weight: 500;
  margin-right: 12px;
}

.item-value.abnormal,
.item-value span.abnormal {
  color: #ee0a24;
}

.item-value small {
  font-size: 12px;
  font-weight: normal;
  color: #999;
  margin-left: 2px;
}

.item-date {
  font-size: 12px;
  color: #999;
}

:deep(.van-dropdown-menu__bar) {
  box-shadow: none;
  height: auto;
}

:deep(.van-dropdown-menu__title) {
  padding: 0;
}
</style>
