"use client";

/**
 * Placeholder for knowledge graph visualization area.
 * 后续会在这里用力导向图等方式展示 Neo4j 图谱。
 */
export function GraphPanel() {
  return (
    <section className="flex h-full flex-col rounded-2xl border border-dashed border-sky-200 bg-sky-50/40 p-4">
      <h2 className="text-xs font-semibold text-sky-800">图谱预览</h2>
      <p className="mt-1 text-xs text-sky-700/80">
        未来这里会展示节点 / 边结构的知识图谱（力导向图）；当前仅为占位组件。
      </p>
    </section>
  );
}

