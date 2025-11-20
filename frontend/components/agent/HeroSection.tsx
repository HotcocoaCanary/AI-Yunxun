'use client';

import { ArrowRight, Loader2 } from 'lucide-react';
import { ChangeEvent, FormEvent } from 'react';

interface HeroSectionProps {
  title: string;
  inputValue: string;
  isLoading: boolean;
  onInputChange: (value: string) => void;
  onSubmit: (event?: FormEvent) => void;
  placeholder?: string;
}

export default function HeroSection({
  title,
  inputValue,
  isLoading,
  onInputChange,
  onSubmit,
  placeholder = '描述你正在研究的课题或问题...',
}: HeroSectionProps) {
  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    onInputChange(event.target.value);
  };

  return (
    <section className="hero-section">
      <div className="hero-card animate-float">
        <p className="hero-title">{title}</p>
        <p className="hero-description">
          向我描述你的研究任务，我会自动协调 MCP 工具、图谱与知识文档来协助你完成下一步。
        </p>
        <form onSubmit={onSubmit} className="hero-input">
          <input
            value={inputValue}
            onChange={handleChange}
            placeholder={placeholder}
            className="hero-input-field"
          />
          <button
            type="submit"
            className="hero-input-action"
            disabled={isLoading || !inputValue.trim()}
          >
            {isLoading ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                正在分析
              </>
            ) : (
              <>
                提交
                <ArrowRight className="h-4 w-4" />
              </>
            )}
          </button>
        </form>
      </div>
    </section>
  );
}
