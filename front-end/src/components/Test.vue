<template>
  <div id="neo4j-d3" @dblclick="onNodeDoubleClick"></div>
</template>

<script>
import {onMounted, ref} from 'vue';
import 'https://d3js.org/d3.v7.min.js';
import '../assets/js/neo4jd3.js';

export default {
  setup() {
    const neo4jData = ref(null);

    const onNodeDoubleClick = (e, d, d3) => {
      console.log(e, d, d3);
    };

    onMounted(async () => {
      try {
        const response = await fetch('http://localhost:2933/Cloud-Hunt-Chart-backend/query', {
          method: 'POST',
          mode: 'no-cors',
        });
        neo4jData.value = await response.json();
        if (neo4jData.value) {
          new Neo4jd3('#neo4j-d3', {
            showNodePlate: true,
            neo4jData: neo4jData.value,
            highlight: [],
            highlightRelationShip: [],
            iconMap: {
              'rich-text': 'e65f',
            },
            onMenuNodeClick({ name, status, metaData }) {
              console.log(name, status, metaData);
            },
            onGetLegend(data) {
              console.log(data);
            },
            onNodeDoubleClick: onNodeDoubleClick,
          });
        }
      } catch (error) {
        console.error('加载JSON文件时出错：', error);
      }
    });

    return {
      neo4jData,
      onNodeDoubleClick,
    };
  },
};
</script>

<style src="../assets/css/neo4jd3.css"></style>
<style>
body {
  margin: 0;
  width: 100vw;
  height: 100vh;
}

#neo4j-d3 {
  width: 100%;
  height: 100%;
}
</style>

