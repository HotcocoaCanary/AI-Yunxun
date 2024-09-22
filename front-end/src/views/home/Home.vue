<template>
  <div id="Neo4jD3">
    <Neo4jD3 :answer="answer"></Neo4jD3>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import Neo4jD3 from "@/components/Graph.vue";

const answer = ref("What are the papers written by Varrelmann");
</script>

<style scoped>
#Neo4jD3 {
  width: 100%;
  height: 100%;
}
</style>
