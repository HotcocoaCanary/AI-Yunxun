<template>
  <el-table :data="computedTableData" style="width: 100%" height="100%">
    <el-table-column
        v-for="(column, index) in computedColumns"
        :key="column.prop"
        :prop="column.prop"
        :label="column.label"
        :width="column.width"
        :fixed="column.fixed"
    />
  </el-table>
</template>

<script lang="ts" setup>
import { defineProps, computed } from 'vue';

const props = defineProps({
  columns: {
    type: Array as () => string[],
    default: () => []
  },
  tableData: {
    type: Array as () => any[],
    default: () => []
  }
});

const computedColumns = computed(() => {
  return props.columns.map((prop, index) => ({
    prop,
    label: prop.charAt(0).toUpperCase() + prop.slice(1),
    width: 'auto',
    fixed: index === 0 ? 'left' : false
  }));
});

const computedTableData = computed(() => {
  return props.tableData.map(row => {
    return row.reduce((acc, cell, index) => {
      acc[props.columns[index]] = cell;
      return acc;
    }, {});
  });
});
</script>
