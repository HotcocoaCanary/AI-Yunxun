<template>
  <el-upload
      v-model:file-list="fileList"
      drag
      multiple
      class="upload-demo"
      action="/api/data/upload"
      :on-change="handleChange"
      :accept="'.xls,.xlsx'"
  >
    <el-icon class="el-icon--upload"><upload-filled /></el-icon>
    <div class="el-upload__text">
      Drop file here or <em>click to upload</em>
    </div>
    <template #tip>
      <div class="el-upload__tip">
        Excel files (.xls, .xlsx) with a size less than 5MB
      </div>
    </template>
  </el-upload>
</template>
<script lang="ts" setup>
import { ref } from 'vue'

import type { UploadProps, UploadUserFile } from 'element-plus'
import {UploadFilled} from "@element-plus/icons-vue";

const fileList = ref<UploadUserFile[]>([

])

const handleChange: UploadProps['onChange'] = (uploadFile, uploadFiles) => {
  fileList.value = fileList.value.slice(-3)
}
</script>
