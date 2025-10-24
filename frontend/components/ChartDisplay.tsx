'use client';

import { useEffect, useRef } from 'react';
import * as echarts from 'echarts';

interface ChartDisplayProps {
  data: any;
}

export default function ChartDisplay({ data }: ChartDisplayProps) {
  const chartRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!chartRef.current || !data) return;

    const chart = echarts.init(chartRef.current);
    
    // 根据数据类型生成不同的图表配置
    const option = generateChartOption(data);
    
    chart.setOption(option);

    // 响应式调整
    const handleResize = () => {
      chart.resize();
    };

    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      chart.dispose();
    };
  }, [data]);

  const generateChartOption = (data: any) => {
    // 根据数据结构生成图表配置
    if (data.type === 'bar') {
      return {
        title: {
          text: data.title || '柱状图',
          left: 'center',
          textStyle: {
            color: '#374151',
            fontSize: 16
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: data.xAxis || [],
          axisLabel: {
            color: '#6B7280'
          }
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            color: '#6B7280'
          }
        },
        series: [{
          data: data.yAxis || [],
          type: 'bar',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#F59E0B' },
              { offset: 1, color: '#D97706' }
            ])
          },
          emphasis: {
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#D97706' },
                { offset: 1, color: '#B45309' }
              ])
            }
          }
        }]
      };
    } else if (data.type === 'pie') {
      return {
        title: {
          text: data.title || '饼图',
          left: 'center',
          textStyle: {
            color: '#374151',
            fontSize: 16
          }
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          textStyle: {
            color: '#6B7280'
          }
        },
        series: [{
          name: data.title || '数据',
          type: 'pie',
          radius: '50%',
          data: data.data || [],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          itemStyle: {
            color: (params: any) => {
              const colors = [
                '#F59E0B', '#D97706', '#B45309', '#92400E', '#78350F',
                '#FCD34D', '#FDE047', '#FACC15', '#EAB308', '#CA8A04'
              ];
              return colors[params.dataIndex % colors.length];
            }
          }
        }]
      };
    } else if (data.type === 'line') {
      return {
        title: {
          text: data.title || '折线图',
          left: 'center',
          textStyle: {
            color: '#374151',
            fontSize: 16
          }
        },
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: data.xAxis || [],
          axisLabel: {
            color: '#6B7280'
          }
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            color: '#6B7280'
          }
        },
        series: [{
          data: data.yAxis || [],
          type: 'line',
          smooth: true,
          lineStyle: {
            color: '#F59E0B',
            width: 3
          },
          itemStyle: {
            color: '#F59E0B'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(245, 158, 11, 0.3)' },
              { offset: 1, color: 'rgba(245, 158, 11, 0.1)' }
            ])
          }
        }]
      };
    } else {
      // 默认柱状图
      return {
        title: {
          text: '数据图表',
          left: 'center',
          textStyle: {
            color: '#374151',
            fontSize: 16
          }
        },
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: ['示例数据1', '示例数据2', '示例数据3'],
          axisLabel: {
            color: '#6B7280'
          }
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            color: '#6B7280'
          }
        },
        series: [{
          data: [120, 200, 150],
          type: 'bar',
          itemStyle: {
            color: '#F59E0B'
          }
        }]
      };
    }
  };

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-4">
      <div ref={chartRef} className="w-full h-80" />
    </div>
  );
}
