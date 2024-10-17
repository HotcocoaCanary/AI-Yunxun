<template>
  <div class="Header">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item
          v-for="(item, index) in breadcrumbItems"
          :key="index"
          :to="item.path">
        {{ item.name }}
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>


<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const breadcrumbItems = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title);
  const homeRoute = { name: '首页', path: '/home' };

  // 创建面包屑数组
  const breadcrumbs = matched.map(item => ({
    name: item.meta.title,
    path: item.path
  }));

  // 如果当前路由是首页，则只显示首页
  if (breadcrumbs.length === 1 && breadcrumbs[0].name === '首页') {
    return breadcrumbs;
  }

  // 如果当前路由不是首页，添加首页到面包屑数组的开始位置
  breadcrumbs.unshift(homeRoute);

  return breadcrumbs;
});
</script>


<style scoped>
.Header {
  padding: 10px 20px;
  font-size: 16px;
}

.Header a {
  text-decoration: none;
  color: #0275d8;
}

.Header a:hover {
  text-decoration: underline;
}

/* 面包屑的样式 */
el-breadcrumb {
  display: inline-block;
}
</style>
