"use client";

import React, { useEffect, useRef } from 'react';
import * as echarts from 'echarts';
import type { EChartsOption } from 'echarts';

interface EChartGraphProps {
    option: EChartsOption;
}

export const EChartGraph: React.FC<EChartGraphProps> = ({ option }) => {
    const chartRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!chartRef.current) return;

        // 初始化图表
        const chartInstance = echarts.init(chartRef.current);
        chartInstance.setOption(option);

        // 响应式处理
        const handleResize = () => chartInstance.resize();
        window.addEventListener('resize', handleResize);

        return () => {
            window.removeEventListener('resize', handleResize);
            chartInstance.dispose();
        };
    }, [option]);

    return (
        <div className="w-full my-4 border rounded-xl bg-white p-4 shadow-sm">
            <div ref={chartRef} style={{ width: '100%', height: '400px' }} />
        </div>
    );
};
