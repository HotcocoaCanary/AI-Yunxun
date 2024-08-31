<template>
  <div ref="chart" style="width: 600px; height: 400px;"></div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: 'EChartsGraph',
  mounted() {
    this.initChart();
  },
  methods: {
    initChart() {
      const chart = echarts.init(this.$refs.chart);
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
            normal: {
              textStyle: {
                fontSize: 20
              }
            }
          },
          force: {
            repulsion: 2500,
            edgeLength: [10, 50]
          },
          draggable: true,
          itemStyle: {
            normal: {
              color: '#4b565b'
            }
          },
          lineStyle: {
            normal: {
              width: 2,
              color: '#e2c08d'
            }
          },
          label: {
            normal: {
              show: true,
              textStyle: {}
            }
          },
          data: [
            {
              name: '123',
              category: 'paper',
              properties: {
                "name": "Zhonghua",
                "Nationality": "China"
              },
            },
            {
              name: '124',
              category: 'author',
              properties: {
                "name": "Zhonghua",
                "Nationality": "China"
              },
            }
          ],
          links: [
            {
              source: '123',
              target: '124',
              name: "AUTHORED",
              position: "43422",
            }
          ],
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

      chart.setOption(option);
    }
  }
}
</script>
