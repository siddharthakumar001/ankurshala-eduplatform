'use client';

import { useState, useCallback } from 'react';

interface FileUploadOptions {
  maxSize?: number; // in bytes
  allowedTypes?: string[];
  multiple?: boolean;
}

interface FileUploadResult {
  file: File | null;
  files: File[];
  error: string | null;
  isUploading: boolean;
  uploadFile: (file: File) => Promise<string | null>;
  uploadFiles: (files: File[]) => Promise<string[]>;
  reset: () => void;
}

export const useFileUpload = (options: FileUploadOptions = {}): FileUploadResult => {
  const [file, setFile] = useState<File | null>(null);
  const [files, setFiles] = useState<File[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isUploading, setIsUploading] = useState(false);

  const validateFile = useCallback((file: File): string | null => {
    if (options.maxSize && file.size > options.maxSize) {
      return `File size must be less than ${Math.round(options.maxSize / 1024 / 1024)}MB`;
    }

    if (options.allowedTypes && !options.allowedTypes.includes(file.type)) {
      return `File type not allowed. Allowed types: ${options.allowedTypes.join(', ')}`;
    }

    return null;
  }, [options]);

  const uploadFile = useCallback(async (file: File): Promise<string | null> => {
    const validationError = validateFile(file);
    if (validationError) {
      setError(validationError);
      return null;
    }

    setIsUploading(true);
    setError(null);

    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await fetch('/api/upload', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error('Upload failed');
      }

      const result = await response.json();
      setFile(file);
      return result.url;
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Upload failed';
      setError(errorMessage);
      return null;
    } finally {
      setIsUploading(false);
    }
  }, [validateFile]);

  const uploadFiles = useCallback(async (files: File[]): Promise<string[]> => {
    const results: string[] = [];
    
    for (const file of files) {
      const result = await uploadFile(file);
      if (result) {
        results.push(result);
      }
    }

    setFiles(files);
    return results;
  }, [uploadFile]);

  const reset = useCallback(() => {
    setFile(null);
    setFiles([]);
    setError(null);
    setIsUploading(false);
  }, []);

  return {
    file,
    files,
    error,
    isUploading,
    uploadFile,
    uploadFiles,
    reset
  };
};
