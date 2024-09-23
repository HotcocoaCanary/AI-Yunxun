<template>
  <div id="chat">
    <div id="question">
      <!-- 使用label元素添加提示语句，并通过flex布局实现样式 -->
      <div class="input-container">
        <label for="chat-input">Ask a question:</label>
        <input id="chat-input" v-model="answer" placeholder="Type your question here..."></input>
      </div>
    </div>
    <div id="graph">
      <GetGraph :answer="answer" method="answer"></GetGraph>
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

.input-container {
  display: flex;
  align-items: center;
  width: 100%;
  margin: 10px 0;
}

.input-container label {
  /* 提示语句样式 */
  margin-right: 10px;
  font-size: 16px;
  color: #555; /* 提示文字颜色 */
}

#chat-input {
  /* 输入框样式 */
  flex-grow: 1;
  padding: 10px;
  font-size: 16px;
  border: 1px solid #ccc; /* 添加边框 */
  border-radius: 4px; /* 圆角边框 */
  outline: none; /* 移除聚焦时的边框 */
}

#chat-input:focus {
  border-color: #007bff; /* 聚焦时的边框颜色 */
}

#graph {
  width: 100%;
  height: 100%; /* 假设高度 */
}
</style>
