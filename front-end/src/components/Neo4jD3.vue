<template>
  <div ref="chart" style="width: 600px; height: 400px;"></div>
</template>

<script>
import * as echarts from 'echarts';
import {getGraph} from "@/api/neo4jService.js";

export default {
  name: 'EChartsGraph',
  props: {
    answer: String
  },
  data() {
    return {
      nodes: [],
      relationships: [],
      chart: null
    };
  },
  watch: {
    answer: {
      immediate: true,
      handler(newValue) {
        if (newValue) {
          this.fetchData(newValue);
        }
      }
    }
  },
  mounted() {
    this.initChart();
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose();
    }
  },
  methods: {
    async fetchData(answer) {
      try {
        let data = await getGraph(answer);
        console.log(data)
        this.nodes = data.nodes;
        this.relationships = data.relationships;
        this.updateChart();
      } catch (error) {
        console.error("获取数据出错", error);
      }
    },
    initChart() {
      this.chart = echarts.init(this.$refs.chart);
      this.updateChart();
    },
    updateChart() {
      if (!this.chart) return;
      const option = {
        title: {
          text: '简单知识图谱示例'
        },
        tooltip: {},
        series: [{
          type: 'graph',
          layout: 'force',
          symbolSize: 45,
          roam: true,
          edgeSymbol: ['circle', 'arrow'],
          edgeSymbolSize: [4, 10],
          edgeLabel: {
            textStyle: {
              fontSize: 20
            }
          },
          force: {
            repulsion: 2500,
            edgeLength: [10, 50]
          },
          draggable: true,
          itemStyle: {
            color: '#4b565b'
          },
          lineStyle: {
            width: 2,
            color: '#e2c08d'
          },
          label: {
            show: true,
            textStyle: {}
          },
          data: this.nodes,
          links: this.relationships,
          categories: [
            {
              name: 'paper'
            },
            {
              name: 'author'
            },
            {
              name: 'country'
            },
            {
              name: 'institution'
            }
          ]
        }]
      };
      this.chart.setOption(option);
    }
  }
}
</script>
