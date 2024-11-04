<template>
  <div class="main-interface">
    <div class="video-overlay"></div>
    <div class="welcome-message">
      <h1>欢迎使用AI云寻问答系统</h1>
      <p>探索知识的海洋，获取您所需的答案。</p>
    </div>
  </div>
</template>

<style scoped>
.main-interface {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 80vh;
  text-align: center;
  overflow: hidden;
  background-image: url('../../assets/img/background.png'); /* 设置背景图片 */
  background-size: cover; /* 覆盖整个容器 */
  background-position: center; /* 居中显示 */
  background-repeat: no-repeat; /* 不重复 */
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5); /* 调整透明度以显示文字 */
  z-index: -1;
}

.welcome-message {
  margin-bottom: 40px;
  z-index: 1; /* 确保文字在覆盖层之上 */
}

h1 {
  font-size: 2.5em;
  color: #027595;
  margin-bottom: 10px;
  animation: slideIn 1s ease-out;
}

p {
  font-size: 1.2em;
  color: #027595;
  animation: slideIn 1.5s ease-out;
}

@keyframes slideIn {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
