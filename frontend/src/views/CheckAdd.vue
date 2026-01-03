<template>
  <div class="page-container">
    <van-nav-bar
      title="添加检查记录"
      left-arrow
      @click-left="router.back()"
    />

    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.itemName"
          is-link
          readonly
          label="检查项目"
          placeholder="选择检查项"
          :rules="[{ required: true, message: '请选择检查项目' }]"
          @click="showItemPicker = true"
        />
        <van-field
          v-model="form.checkDate"
          is-link
          readonly
          label="检查日期"
          placeholder="选择日期"
          :rules="[{ required: true, message: '请选择检查日期' }]"
          @click="showDatePicker = true"
        />
        <van-field
          v-model="form.value"
          type="number"
          label="检查数值"
          :placeholder="`请输入数值${form.unit ? '（' + form.unit + '）' : ''}`"
          :rules="[{ required: true, message: '请输入检查数值' }]"
        />
      </van-cell-group>

      <div class="section-title">报告照片（可选）</div>
      <van-cell-group inset>
        <van-field label="拍照/上传">
          <template #input>
            <van-uploader
              v-model="fileList"
              :max-count="1"
              :after-read="afterRead"
              accept="image/*"
            />
          </template>
        </van-field>
      </van-cell-group>

      <div v-if="selectedItem" class="reference-info">
        <div class="reference-title">参考范围</div>
        <div class="reference-value">
          {{ selectedItem.referenceMin || '-' }} ~ {{ selectedItem.referenceMax || '-' }} {{ selectedItem.unit }}
        </div>
      </div>

      <div class="submit-btn">
        <van-button round block type="primary" native-type="submit" :loading="submitting">
          保存记录
        </van-button>
      </div>
    </van-form>

    <!-- 检查项选择器 -->
    <van-popup v-model:show="showItemPicker" position="bottom">
      <van-picker
        :columns="itemOptions"
        @confirm="onItemConfirm"
        @cancel="showItemPicker = false"
      />
    </van-popup>

    <!-- 日期选择器 -->
    <van-popup v-model:show="showDatePicker" position="bottom">
      <van-date-picker
        v-model="currentDate"
        title="选择日期"
        :max-date="maxDate"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getCheckItems, createCheckRecord, uploadImage } from '@/api'
import dayjs from 'dayjs'

const router = useRouter()
const checkItems = ref([])
const submitting = ref(false)

const form = ref({
  checkItemId: null,
  itemName: '',
  checkDate: dayjs().format('YYYY-MM-DD'),
  value: '',
  unit: '',
  reportImage: ''
})

const fileList = ref([])
const showItemPicker = ref(false)
const showDatePicker = ref(false)
const maxDate = new Date()
const currentDate = ref([
  dayjs().year().toString(),
  (dayjs().month() + 1).toString().padStart(2, '0'),
  dayjs().date().toString().padStart(2, '0')
])

const selectedItem = computed(() =>
  checkItems.value.find(item => item.id === form.value.checkItemId)
)

const itemOptions = computed(() =>
  checkItems.value.map(item => ({
    text: `${item.name}${item.unit ? ' (' + item.unit + ')' : ''}`,
    value: item.id,
    unit: item.unit
  }))
)

const loadItems = async () => {
  try {
    checkItems.value = await getCheckItems()
  } catch (e) {
    console.error(e)
  }
}

const onItemConfirm = ({ selectedOptions }) => {
  form.value.checkItemId = selectedOptions[0].value
  form.value.itemName = selectedOptions[0].text
  form.value.unit = selectedOptions[0].unit || ''
  showItemPicker.value = false
}

const onDateConfirm = ({ selectedValues }) => {
  form.value.checkDate = selectedValues.join('-')
  showDatePicker.value = false
}

const afterRead = async (file) => {
  file.status = 'uploading'
  file.message = '上传中...'

  try {
    const url = await uploadImage(file.file)
    form.value.reportImage = url
    file.status = 'done'
    file.message = ''
  } catch (e) {
    file.status = 'failed'
    file.message = '上传失败'
  }
}

const onSubmit = async () => {
  submitting.value = true

  try {
    await createCheckRecord({
      checkItemId: form.value.checkItemId,
      checkDate: form.value.checkDate,
      value: parseFloat(form.value.value),
      reportImage: form.value.reportImage || null
    })

    showToast('添加成功')
    router.back()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadItems()
})
</script>

<style scoped>
.section-title {
  padding: 16px 16px 8px;
  font-size: 14px;
  color: #969799;
}

.reference-info {
  background: #f0f9eb;
  margin: 16px;
  padding: 12px 16px;
  border-radius: 8px;
}

.reference-title {
  font-size: 12px;
  color: #67c23a;
  margin-bottom: 4px;
}

.reference-value {
  font-size: 14px;
  color: #333;
}

.submit-btn {
  padding: 24px 16px;
}
</style>
