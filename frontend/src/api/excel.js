import axios from 'axios'

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

export const getTemplateList = () => {
  return apiClient.get('/templates/list')
}

export const downloadTemplate = (templateType) => {
  return apiClient.get(`/templates/download/${templateType}`, {
    responseType: 'blob'
  })
}

export const getTemplateColumns = (templateType) => {
  return apiClient.get(`/templates/columns/${templateType}`)
}

export const uploadExcel = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return apiClient.post('/excel/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const getTask = (taskId) => {
  return apiClient.get(`/excel/task/${taskId}`)
}

export const updateRow = (taskId, rowIndex, rowData) => {
  return apiClient.put(`/excel/task/${taskId}/row/${rowIndex}`, rowData)
}

export const deleteRow = (taskId, rowIndex) => {
  return apiClient.delete(`/excel/task/${taskId}/row/${rowIndex}`)
}

export const addRow = (taskId, rowData) => {
  return apiClient.post(`/excel/task/${taskId}/row`, rowData)
}

export const saveTask = (taskId) => {
  return apiClient.post(`/excel/task/${taskId}/save`)
}

export const revalidateTask = (taskId) => {
  return apiClient.post(`/excel/task/${taskId}/revalidate`)
}

export const getValidationResults = (taskId) => {
  return apiClient.get(`/excel/task/${taskId}/validation`)
}

export default {
  getTemplateList,
  downloadTemplate,
  getTemplateColumns,
  uploadExcel,
  getTask,
  updateRow,
  deleteRow,
  addRow,
  saveTask,
  revalidateTask,
  getValidationResults
}
