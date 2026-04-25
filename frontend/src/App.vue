<template>
  <div id="app">
    <el-container>
      <el-header style="background-color: #409EFF; color: white; padding: 0 20px;">
        <div style="font-size: 20px; font-weight: bold; line-height: 60px;">
          Excel数据预览插件
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  min-height: 100vh;
}

.el-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.el-main {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
