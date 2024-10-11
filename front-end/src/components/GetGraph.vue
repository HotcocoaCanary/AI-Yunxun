<script setup>
import {ref, onMounted, defineProps, watch} from 'vue';
import { getAnswerGraph, getAllGraph} from "@/api/neo4jService.js";
import Graph from "@/components/Graph.vue";

const props = defineProps({
  answer: String, // 答案参数，用于getAnswerGraph
});

const nodes = ref([]);
const relationships = ref([]);

onMounted(async () => {
  let data;
  try {
    if(props.answer){
      data = await getAnswerGraph(props.answer);
    }else{
      data = await getAllGraph();
    }
    if (data) {
      nodes.value = data.nodes;
      relationships.value = data.relationships;
    }
  } catch (error) {
    console.error('获取数据出错', error);
  }
});

// 监听answer prop的变化
watch(() => props.answer, async (newAnswer, oldAnswer) => {
  if (newAnswer !== oldAnswer) {
    let data = await getAnswerGraph(newAnswer);
    if (data) {
      nodes.value = data.nodes;
      relationships.value = data.relationships;
    }
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
