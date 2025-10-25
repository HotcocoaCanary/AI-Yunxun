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
      chart: null,
      // 定义节点类别的颜色映射
      nodeCategoryColors: {
        Author: '#FFA500', // 橙色
        Paper: '#4169E1', // 蓝色
        Country: '#589462',
        Institution: '#917634'
      },
      // 定义关系标签的颜色映射
      relationshipLabelColors: {
        AUTHORED: '#FF69B4', // 粉红色
        AFFILIATED_WITH: '#235689',
        LOCATED_IN: '#784512',
        CITES: '#748596'
      }
    };
  },
  computed: {
    computedNodes() {
      return this.nodes.map(node => ({
        // 使用 id 作为图表中节点的唯一标识符
        id: node.id.toString(),
        // 使用 name 作为图表中节点的显示名称
        name: node.properties.name,
        // 其他属性可以根据需要进行添加
        category: node.category[0],
        // 如果需要展示其他属性，可以在这里添加
        nationality: node.properties.Nationality,
        // 注意：这里假设每个节点只有一个类别
        itemStyle: {
          color: this.nodeCategoryColors[node.category[0]] || '#4b565b', // 默认颜色
        },
      }));
    },
    computedRelationships() {
      return this.relationships.map(relationship => ({
        // 使用 source 和 target 作为关系连接的起点和终点
        source: relationship.source.toString(),
        target: relationship.target.toString(),
        // 如果需要显示关系的名称或标签，可以在这里添加
        name: relationship.name,
        label: relationship.label,
        lineStyle: {
          width: 2,
          color: this.relationshipLabelColors[relationship.label] || '#e2c08d', // 默认颜色
        },
      }));
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
