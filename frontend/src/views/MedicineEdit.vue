<template>
  <div class="page-container">
    <van-nav-bar
      :title="isEdit ? '编辑药品' : '添加药品'"
      left-arrow
      @click-left="router.back()"
    />

    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.name"
          label="药品名称"
          placeholder="如：他克莫司"
          :rules="[{ required: true, message: '请填写药品名称' }]"
        />
        <van-field
          v-model="form.specification"
          label="规格"
          placeholder="如：1mg"
        />
      </van-cell-group>

      <div class="section-title">库存信息</div>
      <van-cell-group inset>
        <van-field
          v-model="form.stockQuantity"
          type="digit"
          label="当前库存"
          placeholder="片/粒"
        />
        <van-field
          v-model="form.perBoxQuantity"
          type="digit"
          label="每盒数量"
          placeholder="片/粒"
        />
        <van-field
          v-model="form.purchaseDate"
          is-link
          readonly
          label="购药日期"
          placeholder="点击选择日期"
          @click="showDatePicker = true"
        />
      </van-cell-group>

      <div class="submit-btn">
        <van-button round block type="primary" native-type="submit">
          {{ isEdit ? '保存修改' : '添加药品' }}
        </van-button>
      </div>
    </van-form>

    <van-popup v-model:show="showDatePicker" position="bottom">
      <van-date-picker
        v-model="datePickerValue"
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
import { getMedicine, createMedicine, updateMedicine } from '@/api'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const medicineId = computed(() => route.params.id)
const isEdit = computed(() => !!medicineId.value)

const form = ref({
  name: '',
  specification: '',
  stockQuantity: '',
  perBoxQuantity: '',
  purchaseDate: ''
})

const showDatePicker = ref(false)
const datePickerValue = ref([
  dayjs().year().toString(),
  (dayjs().month() + 1).toString().padStart(2, '0'),
  dayjs().date().toString().padStart(2, '0')
])

const onDateConfirm = ({ selectedValues }) => {
  form.value.purchaseDate = selectedValues.join('-')
  showDatePicker.value = false
}

const loadMedicine = async () => {
  if (!medicineId.value) return
  try {
    const data = await getMedicine(medicineId.value)
    form.value = {
      name: data.name,
      specification: data.specification || '',
      stockQuantity: data.stockQuantity?.toString() || '',
      perBoxQuantity: data.perBoxQuantity?.toString() || '',
      purchaseDate: data.purchaseDate || ''
    }
    if (data.purchaseDate) {
      const parts = data.purchaseDate.split('-')
      datePickerValue.value = parts
    }
  } catch (e) {
    console.error(e)
  }
}

const onSubmit = async () => {
  try {
    const data = {
      name: form.value.name,
      specification: form.value.specification || null,
      stockQuantity: form.value.stockQuantity ? parseInt(form.value.stockQuantity) : 0,
      perBoxQuantity: form.value.perBoxQuantity ? parseInt(form.value.perBoxQuantity) : 0,
      purchaseDate: form.value.purchaseDate || null
    }

    if (isEdit.value) {
      await updateMedicine(medicineId.value, data)
      showToast('修改成功')
    } else {
      await createMedicine(data)
      showToast('添加成功')
    }
    router.back()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadMedicine()
})
</script>

<style scoped>
.section-title {
  padding: 16px 16px 8px;
  font-size: 14px;
  color: #969799;
}

.submit-btn {
  padding: 24px 16px;
}
</style>
