'use client';

import { useState, useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import { Upload, Download, FileText, Database, Trash2, CheckCircle, AlertCircle } from 'lucide-react';

interface FileData {
  id: string;
  name: string;
  size: number;
  type: string;
  status: 'uploading' | 'success' | 'error';
  uploadTime: Date;
}

export default function DataManagement() {
  const [files, setFiles] = useState<FileData[]>([]);
  const [isUploading, setIsUploading] = useState(false);

  const onDrop = useCallback(async (acceptedFiles: File[]) => {
    setIsUploading(true);
    
    for (const file of acceptedFiles) {
      const fileData: FileData = {
        id: Date.now().toString() + Math.random().toString(36).substr(2, 9),
        name: file.name,
        size: file.size,
        type: file.type,
        status: 'uploading',
        uploadTime: new Date()
      };

      setFiles(prev => [...prev, fileData]);

      try {
        // 模拟上传过程
        await new Promise(resolve => setTimeout(resolve, 2000));
        
        // 这里应该调用后端API上传文件
        const formData = new FormData();
        formData.append('file', file);
        
        const response = await fetch('/api/data/upload', {
          method: 'POST',
          body: formData,
        });

        if (response.ok) {
          setFiles(prev => 
            prev.map(f => 
              f.id === fileData.id 
                ? { ...f, status: 'success' as const }
                : f
            )
          );
        } else {
          throw new Error('上传失败');
        }
      } catch (error) {
        console.error('上传失败:', error);
        setFiles(prev => 
          prev.map(f => 
            f.id === fileData.id 
              ? { ...f, status: 'error' as const }
              : f
          )
        );
      }
    }
    
    setIsUploading(false);
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'application/json': ['.json'],
      'text/csv': ['.csv'],
      'application/vnd.ms-excel': ['.xls'],
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': ['.xlsx']
    },
    multiple: true
  });

  const handleDownload = (file: FileData) => {
    // 这里应该调用后端API下载文件
    console.log('下载文件:', file.name);
  };

  const handleDelete = (fileId: string) => {
    setFiles(prev => prev.filter(f => f.id !== fileId));
  };

  const handleExportAll = () => {
    // 导出所有数据为JSON格式
    const data = {
      files: files,
      exportTime: new Date().toISOString(),
      totalFiles: files.length
    };
    
    const dataStr = JSON.stringify(data, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'data-export.json';
    link.click();
    URL.revokeObjectURL(url);
  };

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const getStatusIcon = (status: FileData['status']) => {
    switch (status) {
      case 'uploading':
        return <div className="w-4 h-4 border-2 border-amber-500 border-t-transparent rounded-full animate-spin" />;
      case 'success':
        return <CheckCircle className="w-4 h-4 text-green-500" />;
      case 'error':
        return <AlertCircle className="w-4 h-4 text-red-500" />;
      default:
        return null;
    }
  };

  const getStatusText = (status: FileData['status']) => {
    switch (status) {
      case 'uploading':
        return '上传中...';
      case 'success':
        return '上传成功';
      case 'error':
        return '上传失败';
      default:
        return '';
    }
  };

  return (
    <div className="h-full flex flex-col bg-white">
      {/* 头部 */}
      <div className="border-b border-gray-200 p-4">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold text-gray-900">数据管理</h2>
          <button
            onClick={handleExportAll}
            className="btn-outline flex items-center space-x-2"
          >
            <Download className="w-4 h-4" />
            <span>导出全部</span>
          </button>
        </div>
        
        <p className="text-sm text-gray-600">
          支持上传JSON、CSV、Excel格式的三元组数据文件
        </p>
      </div>

      {/* 上传区域 */}
      <div className="p-4">
        <div
          {...getRootProps()}
          className={`border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors duration-200 ${
            isDragActive
              ? 'border-amber-500 bg-amber-50'
              : 'border-gray-300 hover:border-amber-400 hover:bg-gray-50'
          }`}
        >
          <input {...getInputProps()} />
          <div className="flex flex-col items-center space-y-4">
            <div className="w-16 h-16 bg-amber-100 rounded-full flex items-center justify-center">
              <Upload className="w-8 h-8 text-amber-600" />
            </div>
            <div>
              <p className="text-lg font-medium text-gray-900">
                {isDragActive ? '释放文件以上传' : '拖拽文件到此处或点击选择'}
              </p>
              <p className="text-sm text-gray-500 mt-1">
                支持 JSON、CSV、Excel 格式，最大 10MB
              </p>
            </div>
            <button className="btn-primary">
              选择文件
            </button>
          </div>
        </div>
      </div>

      {/* 文件列表 */}
      <div className="flex-1 overflow-auto p-4">
        {files.length === 0 ? (
          <div className="text-center py-12">
            <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <FileText className="w-8 h-8 text-gray-400" />
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">暂无文件</h3>
            <p className="text-gray-500">上传一些数据文件开始使用</p>
          </div>
        ) : (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-medium text-gray-900">文件列表</h3>
              <span className="text-sm text-gray-500">
                共 {files.length} 个文件
              </span>
            </div>
            
            <div className="space-y-2">
              {files.map((file) => (
                <div
                  key={file.id}
                  className="flex items-center justify-between p-4 bg-gray-50 rounded-lg border border-gray-200"
                >
                  <div className="flex items-center space-x-4">
                    <div className="w-10 h-10 bg-amber-100 rounded-lg flex items-center justify-center">
                      <Database className="w-5 h-5 text-amber-600" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-gray-900 truncate">
                        {file.name}
                      </p>
                      <div className="flex items-center space-x-4 mt-1">
                        <p className="text-xs text-gray-500">
                          {formatFileSize(file.size)}
                        </p>
                        <p className="text-xs text-gray-500">
                          {file.uploadTime.toLocaleString()}
                        </p>
                        <div className="flex items-center space-x-1">
                          {getStatusIcon(file.status)}
                          <span className={`text-xs ${
                            file.status === 'success' ? 'text-green-600' :
                            file.status === 'error' ? 'text-red-600' :
                            'text-amber-600'
                          }`}>
                            {getStatusText(file.status)}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <div className="flex items-center space-x-2">
                    {file.status === 'success' && (
                      <button
                        onClick={() => handleDownload(file)}
                        className="p-2 text-gray-400 hover:text-gray-600 transition-colors duration-200"
                      >
                        <Download className="w-4 h-4" />
                      </button>
                    )}
                    <button
                      onClick={() => handleDelete(file.id)}
                      className="p-2 text-gray-400 hover:text-red-600 transition-colors duration-200"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* 统计信息 */}
      {files.length > 0 && (
        <div className="border-t border-gray-200 p-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="text-center">
              <p className="text-2xl font-bold text-gray-900">{files.length}</p>
              <p className="text-sm text-gray-500">总文件数</p>
            </div>
            <div className="text-center">
              <p className="text-2xl font-bold text-gray-900">
                {files.filter(f => f.status === 'success').length}
              </p>
              <p className="text-sm text-gray-500">成功上传</p>
            </div>
            <div className="text-center">
              <p className="text-2xl font-bold text-gray-900">
                {formatFileSize(files.reduce((sum, f) => sum + f.size, 0))}
              </p>
              <p className="text-sm text-gray-500">总大小</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
