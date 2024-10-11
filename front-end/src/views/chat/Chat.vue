<template>
  <div id="chat">
    <div id="question">
      <input id="chat-input" v-model="answer" placeholder="Type your question here..."></input>
    </div>
    <div id="graph">
      <GetGraph :answer="answer"></GetGraph>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import GetGraph from "@/components/GetGraph.vue";

const answer = ref("What are the papers written by Varrelmann");

</script>

<style scoped>
#chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  height: 100%;
}

#question {
  display: flex;
  align-items: center;
  width: 100%; /* 调整宽度 */
  margin: 10px 0;
}

#chat-input {
  flex-grow: 1;
  width: 100%;
  padding: 10px;
  font-size: 16px;
  border: 1px solid #ccc;
  border-radius: 4px;
  outline: none;
  margin-right: 10px; /* 添加右边距，为按钮留出空间 */
}

#chat-input:focus {
  border-color: #007bff;
}

#graph {
  width: 100%;
  height: 100%; /* 假设高度 */
}
</style>
