<script lang="ts" setup>
import { ref, computed } from 'vue';
import { UploadFilled } from "@element-plus/icons-vue";
import { getPermissions, revertAllUploads, revertUpload, update, getTemplate} from "@/api/neo4jService.js";

const fileList = ref([]);
const password = ref('');
const isLoading = ref(false);
const token = ref('');

const customHeaders = computed(() => ({
  'I_am_the_administrator_of_AI_Yun_xun': token.value
}));

const handleChange = (uploadFile: any) => {
  if (uploadFile.status === 'error') {
    alert('上传失败');
    fileList.value = fileList.value.filter(file => file.uid !== uploadFile.uid);
  }
};

const handleRemove = (file: any) => {
  alert('您确定要删除这个文件吗？');
  fileList.value = fileList.value.filter(fileItem => fileItem.uid !== file.uid);
  const fileName = file.name;
  revertUpload(fileName, token.value);
};

// 假设 getTemplate 和 token 已经定义
const downloadTemplate = async () => {
  try {
    const { code, data, message } = await getTemplate(token.value);
    if (code === 200) {
      // 创建一个临时的a标签来触发下载
      const aElement = document.createElement('a');
      aElement.href = data;
      aElement.download = 'AIYunxun数据模板.xlsx';
      document.body.appendChild(aElement);
      aElement.click();
      document.body.removeChild(aElement);
    } else {
      console.error(message);
      // 处理错误情况
    }
  } catch (error) {
    console.error('Failed to fetch template:', error);
    // 处理异常情况
  }
};

const confirmUpdate = () => {
  if (confirm('您确定要更新文件吗？')) {
    update(token.value)
    // 这里可以添加更新文件的逻辑
    alert('文件更新成功！');
  }
};

const clearAllFiles = () => {
  if (confirm('您确定要清除所有上传的文件吗？')) {
    revertAllUploads(token.value);
    fileList.value = [];
  }
};

const fetchPermissions = async () => {
  isLoading.value = true;
  try {
    const response = await getPermissions(password.value);
    if (response.code === 200) {
      token.value = response.data;
    } else {
      alert('获取权限失败');
    }
  } catch (error) {
    alert('获取权限失败，请重试');
    token.value = '';
  } finally {
    isLoading.value = false;
  }
};
</script>


<template>
  <div class="upload-container">
    <!-- 获取权限部分界面 -->
    <div v-if="!token" class="permission-section">
      <el-input v-model="password" type="password" placeholder="请输入密码" class="password-input"></el-input>

      <el-button :loading="isLoading" @click="fetchPermissions" class="fetch-permission-btn">获取权限</el-button>
    </div>
    <!-- 上传组件 -->
    <div v-else class="upload-section">
      <el-upload
          v-model:file-list="fileList"
          drag
          multiple
          class="upload-demo"
          action="/api/data/upload"
          :on-change="handleChange"
          :on-remove="handleRemove"
          :accept="'.xls,.xlsx'"
          :headers="customHeaders"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传Excel文件（.xls, .xlsx）
          </div>
        </template>
      </el-upload>
      <!-- 按钮区域 -->
      <div class="button-section">
        <div class="tip">
          只能上传Excel文件（.xls, .xlsx）并且要求符合数据模板格式的文件
          。。。
          。。。
        </div>
        <a
            href="#"
            class="el-button el-button--info action-btn"
            download="AIYunxun数据模板.xlsx"
            @click.prevent="downloadTemplate"
        >
          下载数据模板
        </a>
        <el-button type="primary" @click="confirmUpdate" class="action-btn">确认更新</el-button>
        <el-button type="danger" @click="clearAllFiles" class="action-btn">清除所有</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.upload-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.permission-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.password-input {
  width: 300px;
  margin-bottom: 10px;
}

.fetch-permission-btn {
  width: 100px;
}

.upload-section {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.upload-demo {
  width: 80%;
}

.button-section {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  width: 20%;
  padding-left: 20px;
}

.tip {
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
}

.action-btn {
  margin-bottom: 10px;
  width: 100%;
}

.el-upload__text {
  font-size: 16px;
  color: #606266;
}

.el-upload__tip {
  font-size: 14px;
  color: #909399;
}
</style>


