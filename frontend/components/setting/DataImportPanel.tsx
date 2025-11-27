'use client';
import { FormEvent, useRef, useState } from "react";

type DataImportPanelProps = {
  onUploadFile: (file: File) => Promise<void>;
  onSubmitText: (text: string) => Promise<void>;
  onSubmitUrl: (url: string) => Promise<void>;
  loading: boolean;
};

export default function DataImportPanel({
  onUploadFile,
  onSubmitText,
  onSubmitUrl,
  loading,
}: DataImportPanelProps) {
  const [text, setText] = useState("");
  const [url, setUrl] = useState("");
  const [hint, setHint] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement | null>(null);

  const handleFileChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;
    setHint("上传中…");
    try {
      await onUploadFile(file);
      setHint("上传完成");
    } catch (error) {
      setHint("上传失败，请重试");
      console.error(error);
    } finally {
      if (inputRef.current) inputRef.current.value = "";
    }
  };

  const submitText = async (event: FormEvent) => {
    event.preventDefault();
    if (!text.trim()) return;
    setHint("提交文本中…");
    try {
      await onSubmitText(text.trim());
      setText("");
      setHint("文本已提交");
    } catch (error) {
      setHint("提交失败，请重试");
      console.error(error);
    }
  };

  const submitUrl = async (event: FormEvent) => {
    event.preventDefault();
    if (!url.trim()) return;
    setHint("提交 URL 中…");
    try {
      await onSubmitUrl(url.trim());
      setUrl("");
      setHint("URL 已提交");
    } catch (error) {
      setHint("提交失败，请重试");
      console.error(error);
    }
  };

  return (
    <section className="section-card">
      <div className="mb-3">
        <p className="text-xs uppercase tracking-wide text-slate-400">Import</p>
        <h2 className="text-xl font-semibold text-slate-900">数据导入</h2>
        {hint ? <p className="text-xs text-slate-500">{hint}</p> : null}
      </div>
      <div className="grid gap-4 md:grid-cols-2">
        <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50/70 p-4">
          <p className="text-sm font-semibold text-slate-800">上传文件</p>
          <p className="text-xs text-slate-500">支持 JSON / CSV / TXT</p>
          <label className="mt-3 flex cursor-pointer items-center justify-center rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50">
            选择文件
            <input
              ref={inputRef}
              type="file"
              className="hidden"
              onChange={handleFileChange}
              disabled={loading}
            />
          </label>
        </div>

        <form
          onSubmit={submitUrl}
          className="flex flex-col gap-3 rounded-2xl border border-slate-200 bg-slate-50/70 p-4"
        >
          <p className="text-sm font-semibold text-slate-800">提交 URL 让后端抓取</p>
          <input
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="https://example.com/article"
            className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm focus:border-sky-400 focus:outline-none"
          />
          <button
            type="submit"
            className="rounded-lg bg-sky-600 px-3 py-2 text-sm font-semibold text-white hover:bg-sky-700 disabled:opacity-60"
            disabled={loading || !url.trim()}
          >
            提交 URL
          </button>
        </form>
      </div>

      <form
        onSubmit={submitText}
        className="mt-4 rounded-2xl border border-slate-200 bg-slate-50/70 p-4"
      >
        <p className="text-sm font-semibold text-slate-800">粘贴文本</p>
        <textarea
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="可以直接粘贴需要入库或抽取的文本"
          className="mt-2 min-h-[120px] w-full resize-none rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm focus:border-sky-400 focus:outline-none"
        />
        <div className="mt-3 flex justify-end">
          <button
            type="submit"
            className="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 disabled:opacity-60"
            disabled={loading || !text.trim()}
          >
            提交文本
          </button>
        </div>
      </form>
    </section>
  );
}
