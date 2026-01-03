import request from '@/utils/request'

// 药品管理
export const getMedicines = () => request.get('/medicines')
export const getMedicine = (id) => request.get(`/medicines/${id}`)
export const createMedicine = (data) => request.post('/medicines', data)
export const updateMedicine = (id, data) => request.put(`/medicines/${id}`, data)
export const deleteMedicine = (id) => request.delete(`/medicines/${id}`)

// 服药计划
export const getSchedules = () => request.get('/schedules')
export const getSchedule = (id) => request.get(`/schedules/${id}`)
export const createSchedule = (data) => request.post('/schedules', data)
export const updateSchedule = (id, data) => request.put(`/schedules/${id}`, data)
export const deleteSchedule = (id) => request.delete(`/schedules/${id}`)

// 服药记录
export const getTodayMedications = (date) => {
  const params = date ? { date } : {}
  return request.get('/records/today', { params })
}
export const takeMedication = (data) => request.post('/records/take', data)
export const cancelTakeMedication = (data) => request.post('/records/cancel', data)
export const supplementRecord = (data) => request.post('/records/supplement', data)
export const getMedicationHistory = (params) => request.get('/records/history', { params })

// 检查记录
export const getCheckItems = () => request.get('/check-items')
export const getCheckRecords = (params) => request.get('/check-records', { params })
export const createCheckRecord = (data) => request.post('/check-records', data)
export const updateCheckRecord = (id, data) => request.put(`/check-records/${id}`, data)
export const deleteCheckRecord = (id) => request.delete(`/check-records/${id}`)

// 文件上传
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 统计
export const getCompletionRate = (params) => request.get('/statistics/completion-rate', { params })
export const getCheckTrends = (params) => request.get('/statistics/check-trends', { params })
export const getLatestChecks = () => request.get('/statistics/latest-checks')

// 认证
export const login = (data) => request.post('/auth/login', data)
export const logout = () => request.post('/auth/logout')
