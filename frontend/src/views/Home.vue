<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span style="font-size: 18px; font-weight: bold;">
              <i class="el-icon-download" style="margin-right: 8px;"></i>
              下载Excel模板
            </span>
          </div>
          <div class="template-list">
            <div v-for="template in templates" :key="template.type" class="template-item">
              <div class="template-info">
                <div class="template-name">{{ template.name }}</div>
                <div class="template-id">模板ID: {{ template.templateId }}</div>
              </div>
              <el-button 
                type="primary" 
                size="small"
                @click="downloadTemplate(template.type)"
              >
                下载模板
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span style="font-size: 18px; font-weight: bold;">
              <i class="el-icon-upload2" style="margin-right: 8px;"></i>
              上传Excel文件
            </span>
          </div>
          <el-upload
            class="upload-demo"
            drag
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :on-exceed="handleFileExceed"
            :file-list="fileList"
            :limit="1"
            accept=".xlsx,.xls"
            :multiple="false"
          >
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <div class="el-upload__tip" slot="tip">
              只能上传xlsx/xls文件，请先下载对应模板
            </div>
          </el-upload>
          
          <div v-if="selectedFile" class="selected-file-info">
            <el-alert
              title="已选择文件"
              type="info"
              :closable="false"
              show-icon
            >
              <template slot="default">
                <div>文件名: {{ selectedFile.name }}</div>
                <div>文件大小: {{ formatFileSize(selectedFile.size) }}</div>
              </template>
            </el-alert>
            
            <el-button 
              type="primary" 
              size="medium" 
              style="margin-top: 20px; width: 100%;"
              :loading="uploading"
              @click="uploadFile"
            >
              {{ uploading ? '上传中...' : '开始上传并预览' }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span style="font-size: 18px; font-weight: bold;">
              <i class="el-icon-info" style="margin-right: 8px;"></i>
              使用说明
            </span>
          </div>
          <el-steps :active="3" finish-status="success">
            <el-step title="下载模板" description="根据需要下载用户或机构模板"></el-step>
            <el-step title="填写数据" description="按照模板格式填写数据，第一行为模板ID"></el-step>
            <el-step title="上传预览" description="上传Excel文件，系统自动解析并预览数据"></el-step>
            <el-step title="编辑保存" description="在预览页面编辑数据，验证通过后保存"></el-step>
          </el-steps>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getTemplateList, downloadTemplate, uploadExcel } from '@/api/excel'

export default {
  name: 'Home',
  data() {
    return {
      templates: [],
      selectedFile: null,
      fileList: [],
      uploading: false
    }
  },
  mounted() {
    this.loadTemplates()
  },
  methods: {
    async loadTemplates() {
      try {
        const response = await getTemplateList()
        if (response.data.code === 200) {
          this.templates = response.data.data
        } else {
          this.$message.error('加载模板列表失败: ' + response.data.message)
        }
      } catch (error) {
        console.error('加载模板列表失败:', error)
        this.$message.error('加载模板列表失败')
      }
    },
    
    async downloadTemplate(templateType) {
      try {
        const response = await downloadTemplate(templateType)
        const blob = new Blob([response.data], { 
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
        })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        const template = this.templates.find(t => t.type === templateType)
        link.download = template ? `${template.name}.xlsx` : `${templateType}_template.xlsx`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        this.$message.success('模板下载成功')
      } catch (error) {
        console.error('下载模板失败:', error)
        this.$message.error('下载模板失败')
      }
    },
    
    handleFileChange(file, fileList) {
      this.selectedFile = file.raw
      this.fileList = fileList
    },
    
    handleFileRemove(file, fileList) {
      this.selectedFile = null
      this.fileList = fileList
    },
    
    handleFileExceed(files, fileList) {
      this.$message.warning('只能上传一个文件，请先移除已有文件')
    },
    
    resetUploadState() {
      this.selectedFile = null
      this.fileList = []
      this.uploading = false
    },
    
    async uploadFile() {
      if (!this.selectedFile) {
        this.$message.warning('请先选择文件')
        return
      }
      
      this.uploading = true
      
      try {
        const response = await uploadExcel(this.selectedFile)
        if (response.data.code === 200) {
          this.$message.success('上传成功，正在跳转到预览页面...')
          const taskId = response.data.data.taskId
          this.$router.push(`/preview/${taskId}`)
        } else {
          this.$message.error('上传失败: ' + response.data.message)
          this.resetUploadState()
        }
      } catch (error) {
        console.error('上传文件失败:', error)
        if (error.response && error.response.data) {
          this.$message.error('上传失败: ' + (error.response.data.message || '未知错误'))
        } else {
          this.$message.error('上传失败，请检查文件格式')
        }
        this.resetUploadState()
      } finally {
        this.uploading = false
      }
    },
    
    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }
  }
}
</script>

<style scoped>
.home-container {
  max-width: 1400px;
  margin: 0 auto;
}

.box-card {
  margin-bottom: 20px;
}

.template-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.template-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.template-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.template-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.template-id {
  font-size: 12px;
  color: #909399;
}

.selected-file-info {
  margin-top: 20px;
}

.upload-demo {
  margin-top: 10px;
}
</style>
