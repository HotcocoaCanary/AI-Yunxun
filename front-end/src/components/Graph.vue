<template>
  <div ref="chart" class="chart-container" style="width: 100%; height: 100%;" ></div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: 'EChartsGraph',
  props: {
    nodes: Array,
    relationships: Array,
  },
  data() {
    return {
      chart: null
    };
  },
  computed: {
    // 使用计算属性来处理节点和关系数据
    computedNodes() {
      return this.nodes;
    },
    computedRelationships() {
      return this.relationships;
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
    initChart() {
      if (!this.chart) {
        this.chart = echarts.init(this.$refs.chart);
      }
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
          data: this.computedNodes,
          links: this.computedRelationships,
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
  },
  watch: {
    // 监听props变化，当数据更新时重新渲染图表
    computedNodes: {
      handler() {
        this.updateChart();
      },
      deep: true
    },
    computedRelationships: {
      handler() {
        this.updateChart();
      },
      deep: true
    }
  }
}
</script>
