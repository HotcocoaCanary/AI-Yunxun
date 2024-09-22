<script setup>
import Aside from "@/components/Layout/Aside.vue";
import Header from "@/components/Layout/Header.vue";
</script>

<template>
  <div class="common-layout">
    <el-container id="container">
      <el-aside id="aside">
        <Aside/>
      </el-aside>
      <el-container>
        <el-header id="header">
          <Header/>
        </el-header>
        <el-main id="main">
          <router-view></router-view>
        </el-main>
        <el-footer id="footer">creat by @Canary</el-footer>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
.common-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

#container {
  flex: 1;
  display: flex;
  flex-direction: row;
}

#aside {
  width: auto; /* 设置宽度为auto */
  flex: 0 0 auto; /* 关闭flex grow和shrink，基于内容宽度 */
}

#header {
  flex: 0 0 auto;
  background-color: #fff; /* 头部背景色 */
  color: #333; /* 头部文本颜色 */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); /* 头部阴影 */
  border-bottom-left-radius: 8px; /* 头部圆角 */
  border-bottom-right-radius: 8px;
  padding: 10px 20px; /* 头部内边距 */
  width: 100%; /* 头部宽度 */
  z-index: 10; /* 确保头部在其他内容之上 */
}

#main {
  flex: 1;
  background-color: #f8f8f8; /* 主区域背景色 */
  padding: 20px; /* 主区域内边距 */
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05); /* 主区域内部阴影 */
  border-radius: 8px; /* 主区域圆角 */
  overflow: auto; /* 如果内容超出，允许滚动 */
}

#footer {
  flex: 0 0 auto;
  background-color: #333; /* 底部背景色 */
  color: #D7AC70; /* 底部文本颜色 */
  box-shadow: 0 -2px 4px rgba(0, 0, 0, 0.1); /* 底部阴影 */
  padding: 10px 20px; /* 底部内边距 */
  text-align: center; /* 文本居中显示 */
  width: 100%; /* 底部宽度 */
  border-top-left-radius: 8px; /* 底部圆角 */
  border-top-right-radius: 8px;
}

</style>
