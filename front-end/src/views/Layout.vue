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
      <el-container id="right">
        <el-header id="header">
          <Header/>
        </el-header>
        <el-main id="main">
          <router-view></router-view>
        </el-main>
        <el-footer id="footer">creat by @张文博 @彭玺赫</el-footer>
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
  width: 250px; /* 可以设置一个固定宽度，根据实际需求调整 */
  flex: 0 0 auto;
  background-color: #333; /* 侧边栏背景色 */
  color: #fff; /* 侧边栏文本颜色 */
  transition: width 0.3s; /* 平滑过渡效果 */
}

#right {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100vh;
}

#header {
  height: 60px; /* 增加高度，使头部更明显 */
  display: flex;
  align-items: center; /* 垂直居中 */
  background-color: #fff;
  color: #333;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid #e6e6e6; /* 添加底部边框 */
  padding: 0 20px; /* 调整内边距 */
  z-index: 10;
}

.el-breadcrumb {
  font-size: 1.1em; /* 增加面包屑字体大小 */
  margin-left: 10px; /* 添加左边距 */
}

.el-breadcrumb__item {
  color: #666; /* 面包屑文本颜色 */
}

.el-breadcrumb__item a {
  color: #666;
  transition: color 0.3s; /* 平滑过渡效果 */
}

.el-breadcrumb__item a:hover {
  color: #0275d8; /* 悬停时改变颜色 */
  text-decoration: none; /* 移除下划线 */
}

#main {
  flex: 1;
  background-color: #f8f8f8;
  padding: 20px;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  overflow: auto;
}

#footer {
  height: 40px; /* 调整高度 */
  background-color: #333;
  color: #D7AC70;
  box-shadow: 0 -2px 4px rgba(0, 0, 0, 0.1);
  padding: 10px 20px;
  text-align: center;
  border-top: 1px solid #444; /* 添加顶部边框 */
}

</style>
