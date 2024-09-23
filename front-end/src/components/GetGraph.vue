<script setup>
import { ref, onMounted, defineProps } from 'vue';
import { getAnswerGraph, getAllGraph, getSearchGraph } from "@/api/neo4jService.js";
import Graph from "@/components/Graph.vue";

const props = defineProps({
  method: String, // 控制参数，用于选择获取图的方法
  answer: String, // 答案参数，用于getAnswerGraph
  keyword: String // 关键词参数，用于getSearchGraph
});

const nodes = ref([]);
const relationships = ref([]);

onMounted(async () => {
  let data;
  try {
    switch (props.method) {
      case 'answer':
        if (props.answer) {
          data = await getAnswerGraph(props.answer);
        }
        break;
      case 'all':
        data = await getAllGraph();
        break;
      case 'search':
        if (props.keyword) {
          data = await getSearchGraph(props.keyword);
        }
        break;
      default:
        console.error('未知的获取图方法');
        return;
    }
    if (data) {
      nodes.value = data.nodes;
      relationships.value = data.relationships;
    }
  } catch (error) {
    console.error('获取数据出错', error);
  }
});
</script>

<template>
  <div id="graph">
    <Graph :nodes="nodes" :relationships="relationships"></Graph>
  </div>
</template>

<style scoped>
#graph{
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  border-radius: 8px;
}
</style>
