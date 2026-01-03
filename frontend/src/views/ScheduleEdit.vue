<template>
  <div class="page-container">
    <van-nav-bar
      :title="isEdit ? '编辑服药计划' : '添加服药计划'"
      left-arrow
      @click-left="router.back()"
    />

    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.name"
          label="计划名称"
          placeholder="如：早间服药"
          :rules="[{ required: true, message: '请填写计划名称' }]"
        />
        <van-field
          v-model="form.scheduleTimeDisplay"
          is-link
          readonly
          label="服药时间"
          placeholder="选择时间"
          :rules="[{ required: true, message: '请选择服药时间' }]"
          @click="showTimePicker = true"
        />
      </van-cell-group>

      <div class="section-title">服药规律</div>
      <van-cell-group inset>
        <van-field
          v-model="form.scheduleTypeDisplay"
          is-link
          readonly
          label="规律类型"
          placeholder="选择规律类型"
          @click="showTypePicker = true"
        />

        <van-field
          v-if="form.scheduleType === 'INTERVAL'"
          v-model="form.intervalDays"
          type="digit"
          label="间隔天数"
          placeholder="如：3"
        />

        <van-field
          v-if="form.scheduleType === 'WEEKLY'"
          label="选择周几"
        >
          <template #input>
            <van-checkbox-group v-model="selectedWeekDays" direction="horizontal">
              <van-checkbox v-for="day in weekDayOptions" :key="day.value" :name="day.value">
                {{ day.label }}
              </van-checkbox>
            </van-checkbox-group>
          </template>
        </van-field>
      </van-cell-group>

      <div class="section-title">服用药品</div>
      <van-cell-group inset>
        <div class="medicine-table">
          <div class="medicine-header">
            <div class="header-cell medicine-header-cell">药品</div>
            <div class="header-cell dosage-header-cell">剂量</div>
            <div class="header-cell action-header-cell"></div>
          </div>
          <div v-for="(item, index) in medicineItems" :key="index" class="medicine-row">
            <div class="cell medicine-cell">
              <div
                class="medicine-select"
                :class="{ 'placeholder': !item.medicineName }"
                @click="openMedicinePicker(index)"
              >
                {{ item.medicineName || '请选择药品' }}
                <van-icon name="arrow" class="arrow-icon" />
              </div>
            </div>
            <div class="cell dosage-cell">
              <input
                v-model="item.dosage"
                type="number"
                class="dosage-input"
                placeholder="片/粒"
              />
            </div>
            <div class="cell action-cell">
              <van-icon
                v-if="medicineItems.length > 1"
                name="delete-o"
                class="delete-icon"
                @click="removeMedicineItem(index)"
              />
            </div>
          </div>
        </div>
        <van-button size="small" plain type="primary" block @click="addMedicineItem">
          + 添加药品
        </van-button>
      </van-cell-group>

      <div class="section-title">有效期（可选）</div>
      <van-cell-group inset>
        <van-field
          v-model="form.startDate"
          is-link
          readonly
          label="开始日期"
          placeholder="点击选择"
          @click="openDatePicker('start')"
        />
        <van-field
          v-model="form.endDate"
          is-link
          readonly
          label="结束日期"
          placeholder="不填则长期"
          @click="openDatePicker('end')"
        />
      </van-cell-group>

      <div class="submit-btn">
        <van-button round block type="primary" native-type="submit">
          {{ isEdit ? '保存修改' : '添加计划' }}
        </van-button>
      </div>
    </van-form>

    <!-- 时间选择器 -->
    <van-popup v-model:show="showTimePicker" position="bottom">
      <van-time-picker
        v-model="currentTime"
        title="选择时间"
        @confirm="onTimeConfirm"
        @cancel="showTimePicker = false"
      />
    </van-popup>

    <!-- 规律类型选择器 -->
    <van-popup v-model:show="showTypePicker" position="bottom">
      <van-picker
        :columns="scheduleTypeOptions"
        @confirm="onTypeConfirm"
        @cancel="showTypePicker = false"
      />
    </van-popup>

    <!-- 药品选择器 -->
    <van-popup v-model:show="showMedicinePicker" position="bottom">
      <van-picker
        :columns="medicineOptions"
        @confirm="onMedicineConfirm"
        @cancel="showMedicinePicker = false"
      />
    </van-popup>

    <!-- 日期选择器 -->
    <van-popup v-model:show="showDatePicker" position="bottom">
      <van-date-picker
        v-model="currentDate"
        title="选择日期"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getSchedule, createSchedule, updateSchedule, getMedicines } from '@/api'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const scheduleId = computed(() => route.params.id)
const isEdit = computed(() => !!scheduleId.value)

const form = ref({
  name: '',
  scheduleTime: '',
  scheduleTimeDisplay: '',
  scheduleType: 'DAILY',
  scheduleTypeDisplay: '每天',
  intervalDays: '',
  startDate: '',
  endDate: ''
})

const medicines = ref([])
const medicineItems = ref([{ medicineId: null, medicineName: '', dosage: '1' }])
const selectedWeekDays = ref([])

const weekDayOptions = [
  { label: '一', value: '1' },
  { label: '二', value: '2' },
  { label: '三', value: '3' },
  { label: '四', value: '4' },
  { label: '五', value: '5' },
  { label: '六', value: '6' },
  { label: '日', value: '7' }
]

const scheduleTypeOptions = [
  { text: '每天', value: 'DAILY' },
  { text: '每隔N天', value: 'INTERVAL' },
  { text: '每周指定几天', value: 'WEEKLY' }
]

const showTimePicker = ref(false)
const showTypePicker = ref(false)
const showMedicinePicker = ref(false)
const showDatePicker = ref(false)

const currentTime = ref(['08', '00'])
const currentMedicineIndex = ref(0)
const currentDate = ref([
  dayjs().year().toString(),
  (dayjs().month() + 1).toString().padStart(2, '0'),
  dayjs().date().toString().padStart(2, '0')
])
const datePickerType = ref('start')

const medicineOptions = computed(() =>
  medicines.value.map(m => ({
    text: `${m.name} ${m.specification || ''}`,
    value: m.id
  }))
)

const loadMedicines = async () => {
  try {
    medicines.value = await getMedicines()
  } catch (e) {
    console.error(e)
  }
}

const loadSchedule = async () => {
  if (!scheduleId.value) return
  try {
    const data = await getSchedule(scheduleId.value)
    form.value = {
      name: data.name,
      scheduleTime: data.scheduleTime,
      scheduleTimeDisplay: data.scheduleTime,
      scheduleType: data.scheduleType,
      scheduleTypeDisplay: scheduleTypeOptions.find(t => t.value === data.scheduleType)?.text || '每天',
      intervalDays: data.intervalDays?.toString() || '',
      startDate: data.startDate || '',
      endDate: data.endDate || ''
    }

    if (data.scheduleTime) {
      currentTime.value = data.scheduleTime.split(':').slice(0, 2)
    }

    selectedWeekDays.value = data.weekDays ? data.weekDays.split(',') : []

    if (data.medicines && data.medicines.length > 0) {
      medicineItems.value = data.medicines.map(m => ({
        medicineId: m.medicineId,
        medicineName: `${m.medicineName} ${m.specification || ''}`,
        dosage: m.dosage?.toString() || '1'
      }))
    }
  } catch (e) {
    console.error(e)
  }
}

const onTimeConfirm = ({ selectedValues }) => {
  form.value.scheduleTime = selectedValues.join(':')
  form.value.scheduleTimeDisplay = selectedValues.join(':')
  showTimePicker.value = false
}

const onTypeConfirm = ({ selectedOptions }) => {
  form.value.scheduleType = selectedOptions[0].value
  form.value.scheduleTypeDisplay = selectedOptions[0].text
  showTypePicker.value = false
}

const openMedicinePicker = (index) => {
  currentMedicineIndex.value = index
  showMedicinePicker.value = true
}

const onMedicineConfirm = ({ selectedOptions }) => {
  const option = selectedOptions[0]
  medicineItems.value[currentMedicineIndex.value].medicineId = option.value
  medicineItems.value[currentMedicineIndex.value].medicineName = option.text
  showMedicinePicker.value = false
}

const addMedicineItem = () => {
  medicineItems.value.push({ medicineId: null, medicineName: '', dosage: '1' })
}

const removeMedicineItem = (index) => {
  medicineItems.value.splice(index, 1)
}

const openDatePicker = (type) => {
  datePickerType.value = type
  const dateStr = type === 'start' ? form.value.startDate : form.value.endDate
  if (dateStr) {
    currentDate.value = dateStr.split('-')
  }
  showDatePicker.value = true
}

const onDateConfirm = ({ selectedValues }) => {
  const dateStr = selectedValues.join('-')
  if (datePickerType.value === 'start') {
    form.value.startDate = dateStr
  } else {
    form.value.endDate = dateStr
  }
  showDatePicker.value = false
}

const onSubmit = async () => {
  const validMedicines = medicineItems.value.filter(m => m.medicineId)
  if (validMedicines.length === 0) {
    showToast('请至少添加一种药品')
    return
  }

  const data = {
    name: form.value.name,
    scheduleTime: form.value.scheduleTime,
    scheduleType: form.value.scheduleType,
    intervalDays: form.value.intervalDays ? parseInt(form.value.intervalDays) : null,
    weekDays: selectedWeekDays.value.join(',') || null,
    startDate: form.value.startDate || null,
    endDate: form.value.endDate || null,
    isActive: true,
    medicines: validMedicines.map(m => ({
      medicineId: m.medicineId,
      dosage: parseFloat(m.dosage) || 1
    }))
  }

  try {
    if (isEdit.value) {
      await updateSchedule(scheduleId.value, data)
      showToast('修改成功')
    } else {
      await createSchedule(data)
      showToast('添加成功')
    }
    router.back()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadMedicines()
  loadSchedule()
})
</script>

<style scoped>
.section-title {
  padding: 16px 16px 8px;
  font-size: 14px;
  color: #969799;
}

.medicine-table {
  width: 100%;
}

.medicine-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background-color: #f7f8fa;
  border-bottom: 1px solid #eee;
  font-size: 14px;
  font-weight: 500;
  color: #323233;
}

.header-cell {
  padding: 0 8px;
}

.medicine-header-cell {
  flex: 1;
  min-width: 0;
}

.dosage-header-cell {
  flex: 0 0 100px;
  text-align: center;
}

.action-header-cell {
  flex: 0 0 40px;
}

.medicine-row {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #eee;
  min-height: 48px;
}

.medicine-row:last-child {
  border-bottom: none;
}

.cell {
  padding: 12px 16px;
  display: flex;
  align-items: center;
}

.medicine-cell {
  flex: 1;
  min-width: 0;
  padding-right: 8px;
}

.medicine-select {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0;
  font-size: 14px;
  color: #323233;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.medicine-select.placeholder {
  color: #969799;
}

.arrow-icon {
  flex-shrink: 0;
  margin-left: 8px;
  color: #969799;
  font-size: 16px;
}

.dosage-cell {
  flex: 0 0 100px;
  padding: 12px 8px;
  justify-content: center;
}

.dosage-input {
  width: 100%;
  border: none;
  outline: none;
  text-align: center;
  font-size: 14px;
  color: #323233;
  background: transparent;
}

.dosage-input::placeholder {
  color: #c8c9cc;
}

.action-cell {
  flex: 0 0 40px;
  padding: 12px 8px;
  justify-content: center;
}

.delete-icon {
  color: #ee0a24;
  font-size: 20px;
  cursor: pointer;
}

@media (max-width: 375px) {
  .dosage-header-cell,
  .dosage-cell {
    flex: 0 0 80px;
  }
}

.submit-btn {
  padding: 24px 16px;
}

:deep(.van-checkbox-group--horizontal) {
  flex-wrap: wrap;
  gap: 8px;
}
</style>
