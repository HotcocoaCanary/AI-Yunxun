<template>
  <div id="getExcel">
    <div class="button-group-container">
      <el-button-group class="button-group">
        <el-button
            v-for="button in buttons"
            :key="button.text"
            :type="button.type"
            plain
            :class="{ 'is-active': currentButtonType.value === button.text }"
            @click="fetchData(button.text)"
            style="width: 100%"
        >
          {{ button.text }}
        </el-button>
      </el-button-group>
    </div>
    <div class="excel">
      <Excel :columns="tableColumns" :table-data="tableData" v-if="!isLoading" />
      <div v-else>Loading...</div>
    </div>
    <!-- 分页控件 -->
    <div class="pagination-container">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalRows"
      >
      </el-pagination>
    </div>
  </div>
</template>

<script setup>
import Excel from "@/components/Excel.vue";
import { ref } from 'vue';
import { getExcel } from "@/api/neo4jService.js";

const buttons = [
  {type: 'primary', text: 'paper'},
  {type: 'success', text: 'institution'},
  {type: 'warning', text: 'author'},
  {type: 'danger', text: 'country'}
];

const tableColumns = ref([]);
const tableData = ref([]);
const isLoading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const totalRows = ref(0);
const currentButtonType = ref('paper'); // 默认选中'paper'

const fetchData = async (type) => {
  isLoading.value = true;
  currentButtonType.value = type; // 更新当前按钮类型
  try {
    const data = await getExcel(type, currentPage.value, pageSize.value);
    tableColumns.value = data.headers;
    tableData.value = data.data;
    totalRows.value = data.total; // 设置总记录数
  } catch (error) {
    console.error("Error fetching data:", error);
  } finally {
    isLoading.value = false;
  }
};

const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  fetchData(currentButtonType.value);
};

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage;
  fetchData(currentButtonType.value);
};

// Fetch initial data
fetchData(currentButtonType.value);
</script>

<style scoped>
#getExcel{
  height: 100%;
  width: 100%;
}

.button-group-container {
  height: 6%;
  display: flex;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);
}

.excel{
  height: 84%;
}

.button-group {
  width: 100%;
  display: flex;
  justify-content: space-around;
}

.is-active {
  background-color: #409eff;
  color: #fff;
}

.pagination-container {
  height: 10%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);
  display: flex;
  justify-content: center;
}
</style>
