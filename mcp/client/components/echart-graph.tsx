"use client";

import React, { useEffect, useRef } from 'react';
import * as echarts from 'echarts';

interface EChartGraphProps {
    config: string; // 接收 JSON 字符串
}

export const EChartGraph: React.FC<EChartGraphProps> = ({ config }) => {
    const chartRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!chartRef.current) return;

        // 初始化图表
        const chartInstance = echarts.init(chartRef.current);

        try {
            const option = JSON.parse(config);
            chartInstance.setOption(option);
        } catch (error) {
            console.error("EChart 配置解析失败:", error);
        }

        // 响应式处理
        const handleResize = () => chartInstance.resize();
        window.addEventListener('resize', handleResize);

        return () => {
            window.removeEventListener('resize', handleResize);
            chartInstance.dispose();
        };
    }, [config]);

    return (
        <div className="w-full my-4 border rounded-xl bg-white p-4 shadow-sm">
            <div ref={chartRef} style={{ width: '100%', height: '400px' }} />
        </div>
    );
};