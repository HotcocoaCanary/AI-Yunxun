<script setup>
import Upload from "@/components/Upload.vue";
import GetExcel from "@/components/GetExcel.vue";
</script>

<template>
  <div id="maintain">
    <div class="upload">
      <Upload></Upload>
    </div>
    <div class="excel">
      <GetExcel></GetExcel>
    </div>
  </div>
</template>

<style scoped>
#maintain {
  width: 100%;
  height: 100%;
}
.upload{
  height: 30%;
}
.excel{
  height: 70%;
}
</style>
