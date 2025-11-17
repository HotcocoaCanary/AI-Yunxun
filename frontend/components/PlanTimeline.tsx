'use client';

import clsx from 'clsx';

export interface PlanStep {
  id: string;
  title: string;
  tool: string;
  objective: string;
  status: string;
}

interface PlanTimelineProps {
  steps: PlanStep[];
}

export default function PlanTimeline({ steps }: PlanTimelineProps) {
  if (!steps || steps.length === 0) {
    return null;
  }

  return (
    <div className="card space-y-3">
      <div className="flex items-center justify-between">
        <p className="text-sm font-semibold text-gray-900">任务规划</p>
        <span className="text-xs text-gray-500">由 LLM 生成</span>
      </div>
      <div className="space-y-4">
        {steps.map((step, index) => (
          <div key={step.id} className="flex items-start space-x-3">
            <div className="flex flex-col items-center">
              <div
                className={clsx(
                  'w-3 h-3 rounded-full',
                  step.status === 'completed' && 'bg-emerald-500',
                  step.status === 'running' && 'bg-amber-500',
                  step.status !== 'completed' && step.status !== 'running' && 'bg-gray-300'
                )}
              />
              {index !== steps.length - 1 && <div className="w-px flex-1 bg-gray-200 mt-1" />}
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <p className="text-sm font-medium text-gray-900">{step.title}</p>
                <span className="text-[10px] uppercase text-gray-400 border px-1.5 py-0.5 rounded">
                  {step.tool}
                </span>
              </div>
              <p className="text-xs text-gray-500">{step.objective}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
