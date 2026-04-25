<template>
  <div class="preview-container">
    <el-card v-if="loading" class="loading-card">
      <el-skeleton :rows="10" animated />
    </el-card>
    
    <div v-else-if="!task" class="error-container">
      <el-empty description="任务不存在或已过期">
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </el-empty>
    </div>
    
    <div v-else class="content-container">
      <el-card class="header-card">
        <div class="header-content">
          <div class="task-info">
            <h2>数据预览</h2>
            <div class="info-row">
              <el-tag type="info">任务ID: {{ task.taskId }}</el-tag>
              <el-tag type="primary">{{ getTemplateName() }}</el-tag>
              <el-tag :type="hasErrors ? 'danger' : 'success'">
                {{ validCount }} 条有效 / {{ task.dataRows.length }} 条总数据
                <span v-if="hasErrors" style="margin-left: 10px;">
                  ({{ invalidCount }} 条错误)
                </span>
              </el-tag>
            </div>
          </div>
          <div class="header-actions">
            <el-button @click="goBack">
              <i class="el-icon-arrow-left"></i> 返回
            </el-button>
            <el-button type="primary" @click="addNewRow">
              <i class="el-icon-plus"></i> 添加行
            </el-button>
            <el-button type="warning" @click="revalidate">
              <i class="el-icon-refresh"></i> 重新验证
            </el-button>
            <el-button 
              type="success" 
              :disabled="hasErrors"
              @click="saveData"
            >
              <i class="el-icon-check"></i> 保存数据
            </el-button>
          </div>
        </div>
      </el-card>
      
      <el-card v-if="hasErrors" class="error-card">
        <template slot="header">
          <span style="color: #f56c6c; font-weight: bold;">
            <i class="el-icon-warning-outline"></i> 数据验证错误
          </span>
        </template>
        <el-alert
          type="error"
          :closable="false"
          show-icon
        >
          <template slot="title">
            存在 {{ invalidCount }} 条数据验证错误，请先修正后再保存
          </template>
          <template slot="default">
            <div>错误字段会以红色背景高亮显示，鼠标悬停可查看错误信息</div>
          </template>
        </el-alert>
      </el-card>
      
      <el-card class="table-card">
        <div class="table-container">
          <el-table
            :data="task.dataRows"
            border
            stripe
            style="width: 100%"
            :row-class-name="getRowClassName"
          >
            <el-table-column
              type="index"
              label="序号"
              width="80"
              align="center"
            />
            
            <el-table-column
              v-for="(column, field) in columnMapping"
              :key="field"
              :label="field"
              :min-width="150"
            >
              <template slot-scope="scope">
                <div class="cell-container">
                  <el-input
                    v-model="scope.row.data[column]"
                    :class="{'error-input': hasFieldError(scope.row, column)}"
                    size="small"
                    @blur="handleFieldBlur(scope.row, scope.$index, column)"
                    @change="handleFieldChange(scope.row, scope.$index, column)"
                    :placeholder="`请输入${field}`"
                  />
                  <el-tooltip
                    v-if="hasFieldError(scope.row, column)"
                    :content="scope.row.errors[column]"
                    placement="top"
                    effect="dark"
                  >
                    <i class="el-icon-circle-close error-icon"></i>
                  </el-tooltip>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column
              label="状态"
              width="100"
              align="center"
            >
              <template slot-scope="scope">
                <el-tag :type="scope.row.valid ? 'success' : 'danger'" size="small">
                  {{ scope.row.valid ? '有效' : '错误' }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column
              label="操作"
              width="120"
              align="center"
              fixed="right"
            >
              <template slot-scope="scope">
                <el-button
                  type="danger"
                  size="mini"
                  icon="el-icon-delete"
                  @click="handleDelete(scope.$index)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div v-if="task.dataRows.length === 0" class="empty-table">
          <el-empty description="暂无数据">
            <el-button type="primary" @click="addNewRow">添加数据</el-button>
          </el-empty>
        </div>
      </el-card>
      
      <el-dialog
        title="添加新行"
        :visible.sync="addDialogVisible"
        width="600px"
      >
        <el-form
          ref="addForm"
          :model="newRowForm"
          :rules="formRules"
          label-width="100px"
        >
          <el-form-item
            v-for="(column, field) in columnMapping"
            :key="field"
            :label="field"
            :prop="column"
          >
            <el-input
              v-model="newRowForm[column]"
              :placeholder="`请输入${field}`"
            />
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="addDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="confirmAddRow">确 定</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import { 
  getTask, 
  updateRow, 
  deleteRow, 
  addRow, 
  saveTask, 
  revalidateTask,
  getTemplateColumns
} from '@/api/excel'

export default {
  name: 'Preview',
  data() {
    return {
      loading: true,
      task: null,
      columnMapping: {},
      addDialogVisible: false,
      newRowForm: {},
      formRules: {}
    }
  },
  computed: {
    ...mapGetters(['currentTask', 'hasErrors', 'validCount', 'invalidCount'])
  },
  watch: {
    '$route.params.taskId': {
      handler(newTaskId) {
        if (newTaskId) {
          this.loadTask(newTaskId)
        }
      },
      immediate: true
    }
  },
  mounted() {
    const taskId = this.$route.params.taskId
    if (taskId) {
      this.loadTask(taskId)
    }
  },
  methods: {
    ...mapActions(['setCurrentTask', 'updateRow', 'deleteRow', 'addRow', 'clearTask']),
    
    async loadTask(taskId) {
      this.loading = true
      try {
        const response = await getTask(taskId)
        if (response.data.code === 200) {
          this.task = response.data.data
          this.setCurrentTask(this.task)
          await this.loadColumnMapping(this.task.templateType)
          this.initFormRules()
        } else {
          this.$message.error('加载任务失败: ' + response.data.message)
        }
      } catch (error) {
        console.error('加载任务失败:', error)
        this.$message.error('加载任务失败')
      } finally {
        this.loading = false
      }
    },
    
    async loadColumnMapping(templateType) {
      try {
        const response = await getTemplateColumns(templateType)
        if (response.data.code === 200) {
          this.columnMapping = response.data.data.columnMapping
        }
      } catch (error) {
        console.error('加载列映射失败:', error)
      }
    },
    
    initFormRules() {
      this.formRules = {}
      this.newRowForm = {}
      
      for (const field in this.columnMapping) {
        const column = this.columnMapping[field]
        this.newRowForm[column] = ''
        
        if (column === 'userName' || column === 'orgName' || column === 'orgCode') {
          this.formRules[column] = [
            { required: true, message: '请输入必填项', trigger: 'blur' }
          ]
        }
      }
    },
    
    getTemplateName() {
      if (!this.task) return ''
      const typeMap = {
        'user': '用户数据',
        'organization': '机构数据'
      }
      return typeMap[this.task.templateType] || this.task.templateType
    },
    
    getRowClassName({ row }) {
      return row.valid ? '' : 'error-row'
    },
    
    hasFieldError(row, field) {
      return row.errors && row.errors[field]
    },
    
    async handleFieldBlur(row, rowIndex, field) {
      await this.validateAndUpdateRow(row, rowIndex)
    },
    
    async handleFieldChange(row, rowIndex, field) {
    },
    
    async validateAndUpdateRow(row, rowIndex) {
      try {
        const response = await updateRow(this.task.taskId, rowIndex, row)
        if (response.data.code === 200) {
          this.task = response.data.data
          this.setCurrentTask(this.task)
        }
      } catch (error) {
        console.error('更新行失败:', error)
      }
    },
    
    async handleDelete(rowIndex) {
      try {
        await this.$confirm('确定要删除这一行数据吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const response = await deleteRow(this.task.taskId, rowIndex)
        if (response.data.code === 200) {
          this.task = response.data.data
          this.setCurrentTask(this.task)
          this.$message.success('删除成功')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除行失败:', error)
          this.$message.error('删除失败')
        }
      }
    },
    
    addNewRow() {
      this.newRowForm = {}
      for (const field in this.columnMapping) {
        const column = this.columnMapping[field]
        this.newRowForm[column] = ''
      }
      this.addDialogVisible = true
    },
    
    async confirmAddRow() {
      try {
        const newRow = {
          rowIndex: this.task.dataRows.length,
          data: { ...this.newRowForm },
          errors: {},
          valid: true
        }
        
        const response = await addRow(this.task.taskId, newRow)
        if (response.data.code === 200) {
          this.task = response.data.data
          this.setCurrentTask(this.task)
          this.addDialogVisible = false
          this.$message.success('添加成功')
        }
      } catch (error) {
        console.error('添加行失败:', error)
        this.$message.error('添加失败')
      }
    },
    
    async revalidate() {
      try {
        this.loading = true
        const response = await revalidateTask(this.task.taskId)
        if (response.data.code === 200) {
          this.task = response.data.data
          this.setCurrentTask(this.task)
          this.$message.success('重新验证完成')
        }
      } catch (error) {
        console.error('重新验证失败:', error)
        this.$message.error('重新验证失败')
      } finally {
        this.loading = false
      }
    },
    
    async saveData() {
      if (this.hasErrors) {
        this.$message.warning('请先修正所有验证错误后再保存')
        return
      }
      
      try {
        await this.$confirm('确定要保存所有数据吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        })
        
        const response = await saveTask(this.task.taskId)
        if (response.data.code === 200) {
          this.$message.success('保存成功！')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('保存失败:', error)
          if (error.response && error.response.data) {
            this.$message.error('保存失败: ' + (error.response.data.message || '未知错误'))
          } else {
            this.$message.error('保存失败')
          }
        }
      }
    },
    
    goBack() {
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.preview-container {
  max-width: 1600px;
  margin: 0 auto;
}

.loading-card {
  margin-bottom: 20px;
}

.error-container {
  padding: 100px 0;
}

.content-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.header-card {
  border-radius: 8px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.task-info .info-row {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.error-card {
  border-radius: 8px;
  border-left: 4px solid #f56c6c;
}

.table-card {
  border-radius: 8px;
}

.table-container {
  overflow-x: auto;
}

.cell-container {
  position: relative;
  display: flex;
  align-items: center;
  gap: 5px;
}

.error-input {
  background-color: #fef0f0;
  border-color: #f56c6c;
}

.error-input:focus {
  border-color: #f56c6c;
  box-shadow: 0 0 0 2px rgba(245, 108, 108, 0.2);
}

.error-icon {
  color: #f56c6c;
  font-size: 16px;
  cursor: pointer;
}

.empty-table {
  padding: 40px 0;
}

::v-deep .error-row {
  background-color: #fef0f0 !important;
}

::v-deep .error-row:hover > td {
  background-color: #fde2e2 !important;
}

::v-deep .el-table th {
  background-color: #f5f7fa !important;
  font-weight: bold;
}
</style>
