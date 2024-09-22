<script setup>
import {ChatRound, House, PieChart, Search, View} from '@element-plus/icons-vue'
</script>

<template>
  <div class="aside">
    <div class="el-aside-logo"></div>
    <!-- element-plus的菜单标签 -->
    <el-menu active-text-color="#ffd04b" background-color="#1F1F1E" text-color="#fff" router>
      <el-menu-item index="/home">
        <el-icon>
          <House/>
        </el-icon>
        <span>首页</span>
      </el-menu-item>
      <el-sub-menu index="/relation">
        <template #title>
          <el-icon>
            <View/>
          </el-icon>
          <span>关系可视化</span>
        </template>
        <el-menu-item index="/relation/search">
          <el-icon>
            <Search/>
          </el-icon>
          <span>检索关系</span>
        </el-menu-item>
        <el-menu-item index="/relation/overall">
          <el-icon>
            <PieChart/>
          </el-icon>
          <span>关系全貌</span>
        </el-menu-item>
      </el-sub-menu>
      <el-menu-item index="/chat">
        <el-icon>
          <ChatRound/>
        </el-icon>
        <span>问答系统</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<style scoped>
.aside {
  height: 100vh;
  width: 250px;
  background: #1F1F1E;
  display: flex;
  flex-direction: column;
}

.el-aside-logo {
  height: 201px;
  background: url('@/assets/img/logo.png') no-repeat center center;
  background-size: contain;
  width: 100%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3); /* 添加图片阴影 */
  border-radius: 8px; /* 添加图片圆角 */
}
</style>
