<template>
  <body>
  <div id="neo4j-d3"></div>
  </body>
</template>

<script>
import { ref, onMounted } from 'vue';
import Neo4jd3 from '../assets/js/neo4jd3.js';

export default {
  name: 'Neo4jD3',
  setup() {
    const neo4jData = ref({});

    const fetchData = async () => {
      try {
        const response = await fetch('http://localhost:2933/Cloud-Hunt-Chart-backend/query', {
          method: 'POST',
        });
        neo4jData.value = await response.json();
        console.log(response.json());
        initGraph();
      } catch (error) {
        console.error('加载JSON文件时出错：', error);
      }
    };

    const initGraph = () => {
      new Neo4jd3('#neo4j-d3', {
        showNodePlate: true,
        neo4jData: neo4jData.value,
        highlight: [],
        highlightRelationShip: [],
        iconMap: {
          'rich-text': 'e65f',
        },
        onMenuNodeClick({name, status, metaData}) {
          console.log(name, status, metaData);
        },
        onGetLegend(data) {
          console.log(data);
        },
        onNodeDoubleClick(e, d, d3) {
          console.log(e, d, d3);
        }
      });
    };

    onMounted(fetchData);

    return {
      neo4jData
    };
  }
};
</script>

<style src="../assets/css/neo4jd3.css"></style>
<style scoped>
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
